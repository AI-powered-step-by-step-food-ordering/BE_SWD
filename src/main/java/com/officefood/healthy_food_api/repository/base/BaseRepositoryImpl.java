package com.officefood.healthy_food_api.repository.base;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;

public class BaseRepositoryImpl<T, ID> extends SimpleJpaRepository<T, ID>
        implements BaseRepository<T, ID> {

    private final EntityManager em;
    private final JpaEntityInformation<T, ?> info;
    private final Class<T> domainClass;

    public BaseRepositoryImpl(JpaEntityInformation<T, ?> info, EntityManager em) {
        super(info, em);
        this.em = em;
        this.info = info;
        this.domainClass = info.getJavaType();
    }

    private Optional<Field> findActiveField() {
        Field f = ReflectionUtils.findField(domainClass, "isActive");
        if (f != null && (f.getType() == Boolean.class || f.getType() == boolean.class)) return Optional.of(f);
        f = ReflectionUtils.findField(domainClass, "deleted");
        if (f != null && (f.getType() == Boolean.class || f.getType() == boolean.class)) return Optional.of(f);
        f = ReflectionUtils.findField(domainClass, "status");
        return Optional.ofNullable(f);
    }

    @Override
    public int softDeleteById(ID id) {
        return doSoftDelete(Collections.singletonList(id));
    }

    @Override
    public int softDeleteAllById(Collection<ID> ids) {
        return doSoftDelete(ids);
    }

    private int doSoftDelete(Collection<ID> ids) {
        var activeField = findActiveField().orElse(null);
        if (activeField == null) {
            super.deleteAllById(ids);
            return ids.size();
        }
        String entity = info.getEntityName();
        String fieldName = activeField.getName();

        if ("isActive".equals(fieldName)) {
            return em.createQuery("update " + entity + " e set e.isActive = false, e.deletedAt = CURRENT_TIMESTAMP where e.id in :ids")
                    .setParameter("ids", ids)
                    .executeUpdate();
        }
        if ("deleted".equals(fieldName)) {
            return em.createQuery("update " + entity + " e set e.deleted = true where e.id in :ids")
                    .setParameter("ids", ids)
                    .executeUpdate();
        }
        return em.createQuery("update " + entity + " e set e.status = com.officefood.healthy_food_api.model.enums.AccountStatus.DELETED where e.id in :ids")
                .setParameter("ids", ids)
                .executeUpdate();
    }

    @Override
    public int restoreById(ID id) {
        return doRestore(Collections.singletonList(id));
    }

    @Override
    public int restoreAllById(Collection<ID> ids) {
        return doRestore(ids);
    }

    private int doRestore(Collection<ID> ids) {
        var activeField = findActiveField().orElse(null);
        if (activeField == null) return 0;
        String entity = info.getEntityName();
        String fieldName = activeField.getName();

        if ("isActive".equals(fieldName)) {
            return em.createQuery("update " + entity + " e set e.isActive = true, e.deletedAt = null where e.id in :ids")
                    .setParameter("ids", ids)
                    .executeUpdate();
        }
        if ("deleted".equals(fieldName)) {
            return em.createQuery("update " + entity + " e set e.deleted = false where e.id in :ids")
                    .setParameter("ids", ids)
                    .executeUpdate();
        }
        return em.createQuery("update " + entity + " e set e.status = com.officefood.healthy_food_api.model.enums.AccountStatus.ACTIVE where e.id in :ids")
                .setParameter("ids", ids)
                .executeUpdate();
    }

    @Override
    public Page<T> search(String keyword, Pageable pageable) {
        String kw = (keyword == null ? "" : keyword.trim().toLowerCase());

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(domainClass);
        Root<T> root = cq.from(domainClass);
        cq.select(root);

        List<Predicate> predicates = new ArrayList<>();

        if (hasField("name")) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + kw + "%"));
        }
        if (hasField("code")) {
            predicates.add(cb.like(cb.lower(root.get("code")), "%" + kw + "%"));
        }

        // Lọc theo isActive (soft delete)
        if (hasField("isActive")) {
            predicates.add(cb.isTrue(root.get("isActive")));
        }
        if (hasField("deleted")) {
            predicates.add(cb.isFalse(root.get("deleted")));
        }
        if (hasField("status")) {
            try {
                Class<?> statusEnum = root.get("status").getJavaType();
                if (statusEnum.isEnum()) {
                    // Sử dụng reflection để tránh unchecked warning
                    Object[] enumConstants = statusEnum.getEnumConstants();
                    Object deletedEnum = null;
                    for (Object enumConstant : enumConstants) {
                        if ("DELETED".equals(enumConstant.toString())) {
                            deletedEnum = enumConstant;
                            break;
                        }
                    }
                    if (deletedEnum != null) {
                        predicates.add(cb.notEqual(root.get("status"), deletedEnum));
                    }
                }
            } catch (Exception ignored) { }
        }

        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            for (Sort.Order o : pageable.getSort()) {
                orders.add(o.isAscending() ? cb.asc(root.get(o.getProperty())) : cb.desc(root.get(o.getProperty())));
            }
            cq.orderBy(orders);
        }

        TypedQuery<T> query = em.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<T> content = query.getResultList();

        CriteriaQuery<Long> countQ = cb.createQuery(Long.class);
        Root<T> countRoot = countQ.from(domainClass);
        countQ.select(cb.count(countRoot));
        if (!predicates.isEmpty()) {
            countQ.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        long total = em.createQuery(countQ).getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public boolean existsActiveById(ID id) {
        return findActiveById(id).isPresent();
    }

    @Override
    public Optional<T> findActiveById(ID id) {
        var opt = super.findById(id);
        if (opt.isEmpty()) return opt;
        T entity = opt.get();

        if (hasField("isActive")) {
            try {
                Field f = entity.getClass().getDeclaredField("isActive");
                f.setAccessible(true);
                Object v = f.get(entity);
                if (v instanceof Boolean && !(Boolean) v) return Optional.empty();
            } catch (Exception ignored) { }
        }
        if (hasField("deleted")) {
            try {
                Field f = entity.getClass().getDeclaredField("deleted");
                f.setAccessible(true);
                Object v = f.get(entity);
                if (v instanceof Boolean && (Boolean) v) return Optional.empty();
            } catch (Exception ignored) { }
        }
        if (hasField("status")) {
            try {
                Field f = entity.getClass().getDeclaredField("status");
                f.setAccessible(true);
                Object v = f.get(entity);
                if (v != null && "DELETED".equals(String.valueOf(v))) return Optional.empty();
            } catch (Exception ignored) { }
        }
        return Optional.of(entity);
    }

    private boolean hasField(String name) {
        return ReflectionUtils.findField(domainClass, name) != null;
    }
}
