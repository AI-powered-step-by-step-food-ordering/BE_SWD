package com.officefood.healthy_food_api.service;

/**
 * Service for interacting with Google Gemini AI API
 */
public interface GeminiService {

    /**
     * Gửi prompt đơn giản đến Gemini và nhận response
     *
     * @param prompt The prompt to send to Gemini
     * @return The AI response text
     */
    String generateContent(String prompt);

    /**
     * Phân tích bowl dựa trên thông tin bowl và mục tiêu của user
     *
     * @param bowlId ID của bowl cần phân tích
     * @param userMessage Câu hỏi hoặc yêu cầu của người dùng
     * @return Phân tích chi tiết từ AI
     */
    String analyzeBowl(String bowlId, String userMessage);

    /**
     * Chat đơn giản với Gemini (không context bowl)
     *
     * @param message Tin nhắn của người dùng
     * @return Phản hồi từ AI
     */
    String chat(String message);
}

