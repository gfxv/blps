package dev.gfxv.blps.service;


import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    public void notifyUser(Long userId, String message) {
        System.out.println("Уведомление для пользователя " + userId + ": " + message);
    }
}
