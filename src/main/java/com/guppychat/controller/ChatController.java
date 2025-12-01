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
        System.out.println("📨 Mensaje recibido por WebSocket");
        System.out.println("   Emisor: " + chat.getEmisorId());
        System.out.println("   Receptor: " + chat.getReceptorId());
        System.out.println("   Contenido: " + chat.getMensajeCifrado().substring(0, Math.min(50, chat.getMensajeCifrado().length())));

        // Guardar en BD
        Chat mensajeGuardado = chatRepositorio.save(chat);

        System.out.println("💾 Mensaje guardado con ID: " + mensajeGuardado.getId());

        // Enviar al receptor
        plantillaMensajes.convertAndSend("/topic/" + chat.getReceptorId(), mensajeGuardado);
        System.out.println("📤 Mensaje enviado a /topic/" + chat.getReceptorId());

        // También enviar al emisor (para sincronizar)
        plantillaMensajes.convertAndSend("/topic/" + chat.getEmisorId(), mensajeGuardado);
        System.out.println("📤 Mensaje enviado a /topic/" + chat.getEmisorId());
    }

    // Obtener todos los chats de un usuario
    // ✅ CORREGIDO: Agregado ("usuarioId")
    @GetMapping("/chats/{usuarioId}")
    public List<Chat> obtenerChats(@PathVariable("usuarioId") String usuarioId) {
        System.out.println("📖 Obteniendo todos los chats del usuario: " + usuarioId);
        return chatRepositorio.findByEmisorIdOrReceptorId(usuarioId, usuarioId);
    }

    // Obtener mensajes entre dos usuarios específicos
    // ✅ CORREGIDO: Agregado ("usuarioId") y ("otroUsuarioId")
    @GetMapping("/chats/{usuarioId}/conversacion/{otroUsuarioId}")
    public List<Chat> obtenerConversacion(
            @PathVariable("usuarioId") String usuarioId,
            @PathVariable("otroUsuarioId") String otroUsuarioId) {

        System.out.println("========================================");
        System.out.println("📖 OBTENER CONVERSACION");
        System.out.println("   Usuario 1: " + usuarioId);
        System.out.println("   Usuario 2: " + otroUsuarioId);
        System.out.println("========================================");

        try {
            List<Chat> mensajes = chatRepositorio.findConversacion(usuarioId, otroUsuarioId);
            System.out.println("✅ Se encontraron " + mensajes.size() + " mensajes");
            
            // Mostrar algunos mensajes para debug
            for (int i = 0; i < Math.min(3, mensajes.size()); i++) {
                Chat m = mensajes.get(i);
                System.out.println("   Mensaje " + (i+1) + ": de " + m.getEmisorId() + " a " + m.getReceptorId() + " - " + m.getMensajeCifrado());
            }
            
            return mensajes;
        } catch (Exception e) {
            System.err.println("❌ ERROR al obtener conversación: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}