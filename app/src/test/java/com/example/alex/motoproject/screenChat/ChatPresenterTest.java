package com.example.alex.motoproject.screenChat;

import com.example.alex.motoproject.R;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

public class ChatPresenterTest {

    @Mock
    private ChatFragment mView;
    private ChatPresenter chatPresenter = new ChatPresenter(mView);

//    @Captor
//    ArgumentCaptor argCaptor;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testOnEditTextChanged() {
        String[] correctMessages = new String[3];
        correctMessages[0] = "Hello world";
        correctMessages[1] = "H";
        correctMessages[2] = " \n \n \n Hi \n";

        String[] incorrectMessages = new String[2];
        incorrectMessages[0] = "        ";
        incorrectMessages[1] = "\n";

        for (String message : correctMessages) {
            chatPresenter.onEditTextChanged(message);
            verify(mView).enableSendButton();
        }

        for (String message : incorrectMessages) {
            chatPresenter.onEditTextChanged(message);
            verify(mView).disableSendButton();
        }
    }

    @Test
    public void testOnClickSendButton() {
        chatPresenter.onClickSendButton("");
        verify(mView).cleanupEditText();
    }

    @Test
    public void testOnOptionsItemSelected(int itemId) {
        chatPresenter.onOptionsItemSelected(itemId);

        switch (itemId) {
            case R.id.filter_messages_chat:
                verify(mView).showLocLimitDialog();
                break;
        }
    }
}