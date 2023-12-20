package com.reconsale.telegrambot.hadler;

import com.google.protobuf.ByteString;
import com.reconsale.bot.TelegramBot;
import com.reconsale.bot.handler.ConvertAudioToTextHandler;
import com.reconsale.bot.handler.ConvertTextToAudioHandler;
import com.reconsale.bot.handler.FtpUploader;
import com.reconsale.bot.handler.SaveAudioFileHandler;
import com.reconsale.bot.handler.messeageHandler.VoiceMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.reconsale.bot.utils.FtpVariables.FTP_FILE_ADDRESS;

@Component
@RequiredArgsConstructor
public class VoiceMessageHandlerImpl implements VoiceMessage<TelegramBot> {
    @Override
    public void handleVoiceMessage(Message message, long chatId, TelegramBot bot) {

        String fileId = message.getVoice().getFileId();

        try {
            ConvertAudioToTextHandler audioToTextConverter = new ConvertAudioToTextHandler();
            String textResponse = audioToTextConverter.convert(fileId, bot);
            TextMessageHandler textMessageHandler = new TextMessageHandler();
            textMessageHandler.sendTextMessage(chatId, textResponse, bot);
            ConvertTextToAudioHandler textToAudioConverter = new ConvertTextToAudioHandler();
            String remoteFilepathName = null;
            try {
                ByteString bytes = textToAudioConverter.convert(textResponse);
                SaveAudioFileHandler saveToFile = new SaveAudioFileHandler();
                saveToFile.write(bytes);
                FtpUploader ftpUploader = new FtpUploader();
                remoteFilepathName = ftpUploader.uploadFile();
            } catch (Exception e) {
                handleException(e, chatId, bot);
                return;
            }
            sendVoiceMessage(chatId, remoteFilepathName, bot);
        } catch (Exception e) {
            handleException(e, chatId, bot);
        }

    }

    @Override
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

    @Override
    public void sendVoiceMessage(long chatId, String fileName, TelegramBot bot) {
        try {
            SendAudio sendAudio = new SendAudio();
            sendAudio.setChatId(chatId);
            sendAudio.setAudio(new InputFile(FTP_FILE_ADDRESS + fileName));

            bot.execute(sendAudio);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
