# Additional Sequence Diagrams - Bowl & Ingredient Management

## 1. Create Custom Bowl Flow

```plantuml
@startuml
title Create Custom Bowl Flow

actor Customer
participant "BowlController" as Controller
participant "BowlService" as BowlService
participant "BowlTemplateService" as TemplateService
participant "IngredientService" as IngredientService
participant "CategoryService" as CategoryService
participant "Database" as DB

== Browse Bowl Templates ==
Customer -> Controller: GET /api/bowl-templates/getall
activate Controller

Controller -> TemplateService: findAll()
activate TemplateService

TemplateService -> DB: SELECT bt.*, ts.*, c.*, i.*\nFROM bowl_templates bt\nJOIN template_steps ts ON ts.templateId=bt.id\nJOIN categories c ON c.id=ts.categoryId\nLEFT JOIN ingredients i ON i.categoryId=c.id\nWHERE bt.isActive=true\nORDER BY bt.displayOrder, ts.stepOrder

DB --> TemplateService: List<BowlTemplate> with nested steps

TemplateService --> Controller: Templates with categories and ingredients
deactivate TemplateService

Controller --> Customer: 200 OK\n[{templateId, name, basePrice,\nsteps: [{category, ingredients[]}]}]
deactivate Controller

== Select Template and View Details ==
Customer -> Controller: GET /api/bowl-templates/getbyid/{templateId}
activate Controller

Controller -> TemplateService: findById(templateId)
activate TemplateService

TemplateService -> DB: SELECT * FROM bowl_templates\nWHERE id=? AND isActive=true

DB --> TemplateService: BowlTemplate found

TemplateService -> DB: SELECT ts.*, c.name, c.minSelect, c.maxSelect\nFROM template_steps ts\nJOIN categories c ON c.id=ts.categoryId\nWHERE ts.templateId=?\nORDER BY ts.stepOrder

DB --> TemplateService: Template steps with category rules

loop For each category in steps
    TemplateService -> IngredientService: findByCategoryId(categoryId)
    activate IngredientService
    
    IngredientService -> DB: SELECT i.*, ir.*\nFROM ingredients i\nLEFT JOIN ingredient_restrictions ir\n  ON ir.ingredientId=i.id\nWHERE i.categoryId=? AND i.isActive=true

    DB --> IngredientService: Ingredients with restrictions
    deactivate IngredientService
end

TemplateService --> Controller: Complete template with all details
deactivate TemplateService

Controller --> Customer: 200 OK\n{template, steps, ingredients, restrictions}
deactivate Controller

== Build Custom Bowl ==
Customer -> Customer: Select base (e.g., Quinoa Bowl)

loop For each step (category)
    Customer -> Customer: View category ingredients
    
    Customer -> Customer: Check restrictions\n(e.g., vegetarian, gluten-free)
    
    Customer -> Customer: Select ingredients\n(min/max quantities)
    
    note right
      Step 1: Base (min=1, max=1)
        - Brown Rice ($2.00)
      
      Step 2: Protein (min=1, max=2)
        - Grilled Chicken ($4.00)
        - Tofu ($3.00)
      
      Step 3: Vegetables (min=2, max=5)
        - Broccoli ($1.50)
        - Carrots ($1.00)
        - Spinach ($1.50)
      
      Step 4: Sauce (min=1, max=2)
        - Teriyaki ($0.50)
    end note
end

Customer -> Customer: Review selections and total price

Customer -> Customer: Add custom instructions\n(e.g., "No onions, extra sauce")

== Add Bowl to Order ==
Customer -> Controller: POST /api/bowls/create\n{orderId, templateId, name,\ninstruction, items[]}
activate Controller

Controller -> BowlService: create(BowlRequest)
activate BowlService

' Validate template exists
BowlService -> TemplateService: findById(templateId)
activate TemplateService
TemplateService -> DB: SELECT * FROM bowl_templates WHERE id=?
DB --> TemplateService: Template found
deactivate TemplateService

' Validate order exists
BowlService -> DB: SELECT * FROM orders WHERE id=?
DB --> BowlService: Order found

' Validate ingredient selections meet category rules
loop For each category step
    BowlService -> BowlService: countIngredientsInCategory(items, categoryId)
    
    BowlService -> BowlService: validateMinMaxSelection()
    
    alt Validation fails (below min or above max)
        BowlService --> Controller: Exception: Invalid selection
        Controller --> Customer: 400 Bad Request\n"Must select 1-2 proteins"
        stop
    end
end

' Create bowl
BowlService -> DB: BEGIN TRANSACTION

BowlService -> DB: INSERT INTO bowls\n(orderId, templateId, name, instruction, linePrice)
DB --> BowlService: Bowl created

' Add bowl items
loop For each selected ingredient
    BowlService -> IngredientService: findById(ingredientId)
    activate IngredientService
    IngredientService -> DB: SELECT * FROM ingredients WHERE id=?
    DB --> IngredientService: Ingredient with price
    deactivate IngredientService
    
    BowlService -> BowlService: calculateLinePrice(ingredient.price * quantity)
    
    BowlService -> DB: INSERT INTO bowl_items\n(bowlId, ingredientId, quantity, unitPrice, linePrice)
    DB --> BowlService: Item added
end

' Calculate bowl total
BowlService -> BowlService: sumItemPrices()
note right
  Bowl Line Price Calculation:
  basePrice = template.basePrice
  itemsTotal = SUM(item.linePrice)
  bowlTotal = basePrice + itemsTotal
end note

BowlService -> DB: UPDATE bowls SET linePrice=? WHERE id=?

BowlService -> DB: COMMIT TRANSACTION

BowlService --> Controller: Bowl created with items
deactivate BowlService

Controller --> Customer: 201 Created\n{bowlId, name, linePrice, items[]}
deactivate Controller

@enduml
```

## 2. Ingredient Management Flow (Admin)

```plantuml
@startuml
title Ingredient Management Flow - Admin

actor Admin
participant "IngredientController" as Controller
participant "IngredientService" as IngredientService
participant "CategoryService" as CategoryService
participant "IngredientRestrictionService" as RestrictionService
participant "Database" as DB

== Create New Ingredient ==
Admin -> Controller: POST /api/ingredients/create\n{name, categoryId, price, unit,\nnutritionalInfo, isActive}
activate Controller

Controller -> IngredientService: create(IngredientRequest)
activate IngredientService

' Validate category exists
IngredientService -> CategoryService: findById(categoryId)
activate CategoryService
CategoryService -> DB: SELECT * FROM categories WHERE id=?
DB --> CategoryService: Category found
deactivate CategoryService

' Check duplicate name in category
IngredientService -> DB: SELECT COUNT(*) FROM ingredients\nWHERE name=? AND categoryId=?
DB --> IngredientService: count

alt Ingredient name already exists in category
    IngredientService --> Controller: Exception: Duplicate ingredient
    Controller --> Admin: 409 Conflict\n"Ingredient already exists"
    stop
else No duplicate
    ' Create ingredient
    IngredientService -> DB: INSERT INTO ingredients\n(name, categoryId, price, unit,\ncalories, protein, carbs, fat,\nfiber, imageUrl, isActive)
    DB --> IngredientService: Ingredient created
    
    IngredientService --> Controller: IngredientResponse
    deactivate IngredientService
    Controller --> Admin: 201 Created\n{ingredientId, name, price}
    deactivate Controller
end

== Add Ingredient Restrictions ==
Admin -> Controller: POST /api/ingredient-restrictions/create\n{ingredientId, restrictionType,\nrestrictionValue}
activate Controller

Controller -> RestrictionService: create(RestrictionRequest)
activate RestrictionService

RestrictionService -> IngredientService: findById(ingredientId)
activate IngredientService
IngredientService -> DB: SELECT * FROM ingredients WHERE id=?
DB --> IngredientService: Ingredient found
deactivate IngredientService

RestrictionService -> DB: INSERT INTO ingredient_restrictions\n(ingredientId, restrictionType, restrictionValue)
DB --> RestrictionService: Restriction added

note right
  Restriction Types:
  - ALLERGEN (e.g., "peanuts", "dairy")
  - DIETARY (e.g., "vegetarian", "vegan", "gluten-free")
  - HEALTH (e.g., "low-sodium", "low-carb")
end note

RestrictionService --> Controller: RestrictionResponse
deactivate RestrictionService

Controller --> Admin: 201 Created\n{restrictionId, type, value}
deactivate Controller

== Update Ingredient Price ==
Admin -> Controller: PUT /api/ingredients/update/{ingredientId}\n{price: 5.99}
activate Controller

Controller -> IngredientService: update(ingredientId, request)
activate IngredientService

IngredientService -> DB: SELECT * FROM ingredients WHERE id=?
DB --> IngredientService: Current ingredient

IngredientService -> DB: BEGIN TRANSACTION

' Log price change for audit
IngredientService -> DB: INSERT INTO ingredient_price_history\n(ingredientId, oldPrice, newPrice, changedBy, changedAt)

IngredientService -> DB: UPDATE ingredients SET\nprice=?, updatedAt=NOW()\nWHERE id=?
DB --> IngredientService: Ingredient updated

IngredientService -> DB: COMMIT TRANSACTION

IngredientService --> Controller: Updated ingredient
deactivate IngredientService

Controller --> Admin: 200 OK\n{ingredient with new price}
deactivate Controller

== Deactivate Ingredient ==
Admin -> Controller: DELETE /api/ingredients/delete/{ingredientId}
activate Controller

Controller -> IngredientService: deleteById(ingredientId)
activate IngredientService

' Check if ingredient is used in active orders
IngredientService -> DB: SELECT COUNT(*) FROM bowl_items bi\nJOIN bowls b ON b.id=bi.bowlId\nJOIN orders o ON o.id=b.orderId\nWHERE bi.ingredientId=?\nAND o.status NOT IN ('COMPLETED', 'CANCELLED')
DB --> IngredientService: activeOrdersCount

alt Ingredient in active orders
    IngredientService --> Controller: Exception: Cannot delete
    Controller --> Admin: 400 Bad Request\n"Ingredient is used in active orders"
    stop
else No active orders
    ' Soft delete
    IngredientService -> DB: UPDATE ingredients SET\nisActive=false,\ndeletedAt=NOW()\nWHERE id=?
    DB --> IngredientService: Ingredient deactivated
    
    IngredientService --> Controller: Success
    deactivate IngredientService
    Controller --> Admin: 200 OK\n"Ingredient deactivated"
    deactivate Controller
end

@enduml
```

## 3. Category Management Flow

```plantuml
@startuml
title Category Management Flow

actor Admin
participant "CategoryController" as Controller
participant "CategoryService" as CategoryService
participant "IngredientService" as IngredientService
participant "Database" as DB

== Create Category ==
Admin -> Controller: POST /api/categories/create\n{name, description, displayOrder,\nminSelect, maxSelect}
activate Controller

Controller -> CategoryService: create(CategoryRequest)
activate CategoryService

CategoryService -> DB: SELECT COUNT(*) FROM categories\nWHERE name=?
DB --> CategoryService: count

alt Category name exists
    CategoryService --> Controller: Exception: Duplicate name
    Controller --> Admin: 409 Conflict
    stop
else Name available
    CategoryService -> DB: INSERT INTO categories\n(name, description, displayOrder,\nminSelect, maxSelect, isActive)
    DB --> CategoryService: Category created
    
    note right
      Category Rules:
      - minSelect: Minimum ingredients to select (e.g., 1)
      - maxSelect: Maximum ingredients to select (e.g., 3)
      - Used for bowl building validation
    end note
    
    CategoryService --> Controller: CategoryResponse
    deactivate CategoryService
    Controller --> Admin: 201 Created\n{categoryId, name, minSelect, maxSelect}
    deactivate Controller
end

== Update Category Order ==
Admin -> Controller: PUT /api/categories/update/{categoryId}\n{displayOrder: 2}
activate Controller

Controller -> CategoryService: update(categoryId, request)
activate CategoryService

CategoryService -> DB: UPDATE categories SET\ndisplayOrder=?,\nupdatedAt=NOW()\nWHERE id=?
DB --> CategoryService: Category updated

CategoryService --> Controller: Updated category
deactivate CategoryService

Controller --> Admin: 200 OK\n{category}
deactivate Controller

== View Category with Ingredients ==
Admin -> Controller: GET /api/categories/getbyid/{categoryId}
activate Controller

Controller -> CategoryService: findById(categoryId)
activate CategoryService

CategoryService -> DB: SELECT * FROM categories WHERE id=?
DB --> CategoryService: Category

CategoryService -> IngredientService: findByCategoryId(categoryId)
activate IngredientService

IngredientService -> DB: SELECT i.*, ir.*\nFROM ingredients i\nLEFT JOIN ingredient_restrictions ir\n  ON ir.ingredientId=i.id\nWHERE i.categoryId=?\nORDER BY i.name

DB --> IngredientService: Ingredients with restrictions
deactivate IngredientService

CategoryService --> Controller: Category with ingredients
deactivate CategoryService

Controller --> Admin: 200 OK\n{category, ingredients[]}
deactivate Controller

@enduml
```

## 4. Bowl Template Creation Flow (Admin)

```plantuml
@startuml
title Bowl Template Creation Flow - Admin

actor Admin
participant "BowlTemplateController" as Controller
participant "BowlTemplateService" as TemplateService
participant "TemplateStepService" as StepService
participant "CategoryService" as CategoryService
participant "Database" as DB

== Create Bowl Template ==
Admin -> Controller: POST /api/bowl-templates/create\n{name, description, basePrice,\nimageUrl, displayOrder, isActive}
activate Controller

Controller -> TemplateService: create(BowlTemplateRequest)
activate TemplateService

TemplateService -> DB: INSERT INTO bowl_templates\n(name, description, basePrice,\nimageUrl, displayOrder, isActive)
DB --> TemplateService: Template created

TemplateService --> Controller: BowlTemplateResponse
deactivate TemplateService

Controller --> Admin: 201 Created\n{templateId, name, basePrice}
deactivate Controller

== Add Template Steps ==
Admin -> Controller: POST /api/template-steps/create\n{templateId, categoryId, stepOrder,\ninstruction}
activate Controller

Controller -> StepService: create(TemplateStepRequest)
activate StepService

' Validate template exists
StepService -> TemplateService: findById(templateId)
activate TemplateService
TemplateService -> DB: SELECT * FROM bowl_templates WHERE id=?
DB --> TemplateService: Template found
deactivate TemplateService

' Validate category exists
StepService -> CategoryService: findById(categoryId)
activate CategoryService
CategoryService -> DB: SELECT * FROM categories WHERE id=?
DB --> CategoryService: Category found
deactivate CategoryService

' Create template step
StepService -> DB: INSERT INTO template_steps\n(templateId, categoryId, stepOrder, instruction)
DB --> StepService: Step created

note right
  Example Template Steps:
  
  Template: "Build Your Bowl"
  
  Step 1: Base (order=1)
    - Category: Grains
    - Instruction: "Choose your base"
  
  Step 2: Protein (order=2)
    - Category: Proteins
    - Instruction: "Select 1-2 proteins"
  
  Step 3: Vegetables (order=3)
    - Category: Vegetables
    - Instruction: "Pick 3-5 veggies"
  
  Step 4: Toppings (order=4)
    - Category: Toppings
    - Instruction: "Add toppings"
  
  Step 5: Sauce (order=5)
    - Category: Sauces
    - Instruction: "Choose your sauce"
end note

StepService --> Controller: TemplateStepResponse
deactivate StepService

Controller --> Admin: 201 Created\n{stepId, categoryName, stepOrder}
deactivate Controller

Admin -> Admin: Repeat for each category

== View Complete Template ==
Admin -> Controller: GET /api/bowl-templates/getbyid/{templateId}
activate Controller

Controller -> TemplateService: findById(templateId)
activate TemplateService

TemplateService -> DB: SELECT bt.*, ts.*, c.*\nFROM bowl_templates bt\nLEFT JOIN template_steps ts ON ts.templateId=bt.id\nLEFT JOIN categories c ON c.id=ts.categoryId\nWHERE bt.id=?\nORDER BY ts.stepOrder

DB --> TemplateService: Template with ordered steps

TemplateService --> Controller: Complete template
deactivate TemplateService

Controller --> Admin: 200 OK\n{template with steps hierarchy}
deactivate Controller

== Activate Template ==
Admin -> Controller: PUT /api/bowl-templates/update/{templateId}\n{isActive: true}
activate Controller

Controller -> TemplateService: update(templateId, request)
activate TemplateService

' Validate template has steps
TemplateService -> DB: SELECT COUNT(*) FROM template_steps\nWHERE templateId=?
DB --> TemplateService: stepCount

alt No steps defined
    TemplateService --> Controller: Exception: Template incomplete
    Controller --> Admin: 400 Bad Request\n"Template must have at least one step"
    stop
else Has steps
    TemplateService -> DB: UPDATE bowl_templates SET\nisActive=true,\nupdatedAt=NOW()\nWHERE id=?
    DB --> TemplateService: Template activated
    
    TemplateService --> Controller: Updated template
    deactivate TemplateService
    Controller --> Admin: 200 OK\n"Template is now active"
    deactivate Controller
end

@enduml
```


