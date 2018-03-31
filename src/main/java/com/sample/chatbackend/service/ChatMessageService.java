package com.sample.chatbackend.service;

import com.sample.chatbackend.model.ChatMessage;
import com.sample.chatbackend.repository.ChatMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class ChatMessageService {

    private final Logger log = LoggerFactory.getLogger(ChatMessageService.class);

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    /**
     * Save a chatMessage.
     *
     * @param chatMessage the entity to save
     * @return the saved entity
     */
    public ChatMessage save(ChatMessage chatMessage) {
        log.debug("Request to save ChatMessage : {}", chatMessage);
        return chatMessageRepository.save(chatMessage);
    }

    /**
     * Get all the chatMessages.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<ChatMessage> findAll(Pageable pageable) {
        log.debug("Request to get all ChatMessages");
        return chatMessageRepository.findAll(pageable);
    }

}
