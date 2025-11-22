package com.guppychat.controller;

import com.guppychat.model.Chat;
import com.guppychat.repository.ChatRepositorio;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

//@RestController
//@CrossOrigin(origins = "http://localhost:5173")
public class ChatControllerBackup {

    private final SimpMessagingTemplate plantillaMensajes;
    private final ChatRepositorio chatRepositorio;

    public ChatControllerBackup(SimpMessagingTemplate plantillaMensajes, ChatRepositorio chatRepositorio) {
        this.plantillaMensajes = plantillaMensajes;
        this.chatRepositorio = chatRepositorio;
    }

    @MessageMapping("/chat.enviar")
    public void enviar(Chat chat) {
        chatRepositorio.save(chat);
        plantillaMensajes.convertAndSend("/tema/" + chat.getReceptorId(), chat);
    }

    // Endpoint REST para obtener chats por usuario (opcional)
    @GetMapping("/chats/{usuarioId}")
    public java.util.List<Chat> obtenerChats(@PathVariable String usuarioId) {
        return chatRepositorio.findByEmisorIdOrReceptorId(usuarioId, usuarioId);
    }
}
