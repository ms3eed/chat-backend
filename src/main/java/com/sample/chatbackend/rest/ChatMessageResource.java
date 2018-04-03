package com.sample.chatbackend.rest;

import com.sample.chatbackend.model.ChatMessage;
import com.sample.chatbackend.service.ChatMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api")
public class ChatMessageResource {

    private final Logger log = LoggerFactory.getLogger(ChatMessageResource.class);

    private final ChatMessageService chatMessageService;

    public ChatMessageResource(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    /**
     * POST  /chat-messages : Create a new chatMessage.
     *
     * @param chatMessage the chatMessage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new chatMessage
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/chat-messages")
    public ResponseEntity<ChatMessage> createChatMessage(@Valid @RequestBody ChatMessage chatMessage) throws URISyntaxException {
        log.debug("REST request to save ChatMessage : {}", chatMessage);
        chatMessage.setCreatedAt(ZonedDateTime.now());
        ChatMessage result = chatMessageService.save(chatMessage);
        return ResponseEntity.created(new URI("/api/chat-messages/" + result.getId()))
            .body(result);
    }

    /**
     * GET  /chat-messages : get all the chatMessages.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of chatMessages in body
     */
    @GetMapping("/chat-messages")
    public ResponseEntity<List<ChatMessage>> getAllChatMessages(Pageable pageable) {
        log.debug("REST request to get a page of ChatMessages");
        Page<ChatMessage> page = chatMessageService.findAll(pageable);
        //HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/chat-messages");
        return new ResponseEntity<>(page.getContent(), HttpStatus.OK);
    }

}
