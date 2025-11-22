package com.guppychat.controller;

import com.guppychat.model.Chat;
import com.guppychat.repository.ChatRepositorio;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class ChatController {
    
    private final SimpMessagingTemplate plantillaMensajes;
    private final ChatRepositorio chatRepositorio;

    public ChatController(SimpMessagingTemplate plantillaMensajes, ChatRepositorio chatRepositorio) {
        this.plantillaMensajes = plantillaMensajes;
        this.chatRepositorio = chatRepositorio;
    }

    @MessageMapping("/chat.enviar")
    public void enviar(Chat chat) {
        System.out.println("Mensaje recibido - Emisor: " + chat.getEmisorId() + ", Receptor: " + chat.getReceptorId());
        
        // Guardar en BD
        Chat mensajeGuardado = chatRepositorio.save(chat);
        
        System.out.println("Mensaje guardado con ID: " + mensajeGuardado.getId());
        
        // Enviar al receptor
        plantillaMensajes.convertAndSend("/topic/" + chat.getReceptorId(), mensajeGuardado);
        
        // También enviar al emisor (para sincronizar en otros dispositivos si fuera necesario)
        plantillaMensajes.convertAndSend("/topic/" + chat.getEmisorId(), mensajeGuardado);
        
        System.out.println("Mensaje enviado a topics");
    }

    // Obtener todos los chats de un usuario
    @GetMapping("/chats/{usuarioId}")
    public List<Chat> obtenerChats(@PathVariable String usuarioId) {
        return chatRepositorio.findByEmisorIdOrReceptorId(usuarioId, usuarioId);
    }
    
    // NUEVO: Obtener mensajes entre dos usuarios específicos
    @GetMapping("/chats/{usuarioId}/conversacion/{otroUsuarioId}")
    public List<Chat> obtenerConversacion(
            @PathVariable String usuarioId,
            @PathVariable String otroUsuarioId) {
        
        // Obtener mensajes donde usuarioId es emisor Y otroUsuarioId es receptor
        // O donde usuarioId es receptor Y otroUsuarioId es emisor
        return chatRepositorio.findConversacion(usuarioId, otroUsuarioId);
    }
}