package com.reconsale.telegrambot.hadler;

import com.reconsale.bot.TelegramBot;

import com.reconsale.bot.handler.messeageHandler.TextMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
public class TextMessageHandler implements TextMessage<TelegramBot> {

    public void handleTextMessage(Message message, long chatId, TelegramBot bot) {
        String messageText = message.getText();
        sendTextMessage(chatId, messageText, bot);

    }

    public void sendTextMessage(long chatId, String messageText, TelegramBot bot) {
        SendMessage textResponse = new SendMessage();
        textResponse.setChatId(chatId);
        textResponse.setText("Вы прислали мені: " + messageText);

        try {
            bot.execute(textResponse);
        } catch (TelegramApiException e) {
            handleException(e, chatId, bot);
        }
    }

    public void handleException(Exception e, long chatId, TelegramBot bot) {
        e.printStackTrace();

        SendMessage errorMessage = new SendMessage();
        errorMessage.setChatId(chatId);
        errorMessage.setText("Сталась помилка при обробці запиту, спробуйте пізніше");

        try {
            bot.execute(errorMessage);
        } catch (TelegramApiException ex) {
            ex.printStackTrace();
        }
    }

}
