package com.guppychat.service;

import com.guppychat.model.Chat;
import com.guppychat.repository.ChatRepositorio;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ChatServicio {

    private final ChatRepositorio chatRepositorio;

    public ChatServicio(ChatRepositorio chatRepositorio) {
        this.chatRepositorio = chatRepositorio;
    }

    public List<Chat> obtenerChatsDeUsuario(String usuarioId) {
        return chatRepositorio.findByEmisorIdOrReceptorId(usuarioId, usuarioId);
    }
}
