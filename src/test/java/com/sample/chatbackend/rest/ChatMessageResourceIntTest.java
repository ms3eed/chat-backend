package com.sample.chatbackend.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sample.chatbackend.ChatbackendApplication;
import com.sample.chatbackend.model.ChatMessage;
import com.sample.chatbackend.repository.ChatMessageRepository;
import com.sample.chatbackend.service.ChatMessageService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the ChatMessageResource REST controller.
 *
 * @see ChatMessageResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ChatbackendApplication.class)
public class ChatMessageResourceIntTest {

    private static final String DEFAULT_MESSAGE = "Test Message";

    private static final String DEFAULT_USER_NAME = "Test User";

    private static final ZonedDateTime DEFAULT_CREATED_AT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);

    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(
            MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(), StandardCharsets.UTF_8);

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private EntityManager em;


    private MockMvc restChatMessageMockMvc;

    private ChatMessage chatMessage;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final ChatMessageResource chatMessageResource = new ChatMessageResource(chatMessageService);
        this.restChatMessageMockMvc = MockMvcBuilders.standaloneSetup(chatMessageResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter)
            .build();
    }

    /**
     * Create an entity for tests.
     *
     */
    public static ChatMessage createEntity(EntityManager em) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessage(DEFAULT_MESSAGE);
        chatMessage.setUserName(DEFAULT_USER_NAME);
        chatMessage.setCreatedAt(DEFAULT_CREATED_AT);
        return chatMessage;
    }

    @Before
    public void initTest() {
        chatMessage = createEntity(em);
    }

    @Test
    @Transactional
    public void createChatMessage() throws Exception {
        int databaseSizeBeforeCreate = chatMessageRepository.findAll().size();
        restChatMessageMockMvc.perform(post("/api/chat-messages")
            .contentType(APPLICATION_JSON_UTF8)
            .content(convertObjectToJsonBytes(chatMessage)))
            .andExpect(status().isBadRequest());

        // Validate the ChatMessage in the database
        List<ChatMessage> chatMessageList = chatMessageRepository.findAll();
        assertThat(chatMessageList).hasSize(databaseSizeBeforeCreate + 1);
        ChatMessage testChatMessage = chatMessageList.get(chatMessageList.size() - 1);
        assertThat(testChatMessage.getMessage()).isEqualTo(DEFAULT_MESSAGE);
        assertThat(testChatMessage.getUserName()).isEqualTo(DEFAULT_USER_NAME);
        assertThat(testChatMessage.getCreatedAt()).isEqualTo(DEFAULT_CREATED_AT);
    }

    @Test
    @Transactional
    public void checkUserNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = chatMessageRepository.findAll().size();
        chatMessage.setUserName(null);

        // Create the ChatMessage, which fails.
        restChatMessageMockMvc.perform(post("/api/chat-messages")
            .contentType(APPLICATION_JSON_UTF8)
            .content(convertObjectToJsonBytes(chatMessage)))
            .andExpect(status().isBadRequest());

        List<ChatMessage> chatMessageList = chatMessageRepository.findAll();
        assertThat(chatMessageList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllChatMessages() throws Exception {
        chatMessageRepository.saveAndFlush(chatMessage);
        // Get all the chatMessageList
        restChatMessageMockMvc.perform(get("/api/chat-messages?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(chatMessage.getId().intValue())))
            .andExpect(jsonPath("$.[*].message").value(hasItem(DEFAULT_MESSAGE.toString())))
            .andExpect(jsonPath("$.[*].userName").value(hasItem(DEFAULT_USER_NAME.toString())));
    }


    public static byte[] convertObjectToJsonBytes(Object object)
            throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        JavaTimeModule module = new JavaTimeModule();
        mapper.registerModule(module);
        return mapper.writeValueAsBytes(object);
    }


}
