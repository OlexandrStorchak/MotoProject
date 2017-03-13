package com.example.alex.motoproject.screenChat;

import android.view.View;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.event.ConfirmShareLocationInChatEvent;
import com.example.alex.motoproject.event.GpsStatusChangedEvent;
import com.example.alex.motoproject.event.OnClickChatDialogFragmentEvent;
import com.example.alex.motoproject.event.ShareLocationInChatAllowedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

public class ChatPresenter implements ChatMvp.ViewToPresenter, ChatMvp.ModelToPresenter {

    private WeakReference<ChatMvp.PresenterToView> mView;
    private ChatMvp.PresenterToModel mModel = new ChatModel(this);

    private int mDistanceLimit;

    @Inject
    public ChatPresenter(ChatMvp.PresenterToView view) {
        mView = new WeakReference<>(view);
    }

    private ChatMvp.PresenterToView getView() throws NullPointerException {
        if (mView != null) {
            return mView.get();
        } else {
            throw new NullPointerException("View is unavailable");
        }
    }

    @Override
    public void onEditTextTextChanged(CharSequence charSequence) {
        if (charSequence.length() > 0 && !charSequence.toString().matches("\\s+")) {
            getView().enableSendButton();
        } else {
            getView().disableSendButton();
        }
    }

    @Override
    public void onClickSendButton(String msg) {
        msg = msg.trim().replaceAll(" +", " ");
        msg = msg.replaceAll("\\n+", "\n");
        mModel.sendChatMessage(msg);
        getView().scrollToPosition(mModel.getMessagesSize());
        getView().cleanupEditText();
    }

    @Override
    public void onClickShareLocationButton() {
        EventBus.getDefault().post(new ConfirmShareLocationInChatEvent());
    }

    @Override
    public void onTouchRecyclerView(View view) {
        getView().hideKeyboard(view);
    }

    private void registerChatMessagesListener() {
        mModel.registerChatMessagesListener();
    }

    private void unregisterChatMessagesListener() {
        mModel.unregisterChatMessagesListener();
    }

    private void registerAdapter() {
        getView().setListToAdapter(mModel.getMessages());
    }

    @Override
    public void onRefreshSwipeLayout() {
        mModel.fetchOlderChatMessages();
    }

    @Override
    public void onOptionsItemSelected(int itemId) {
        // TODO: 12.03.2017 maybe use an interface here
        switch (itemId) {
            case R.id.filter_messages_chat:
                getView().showLocLimitDialog();
                break;
        }

//        int meters;
//        switch (itemId) {
//            case R.id.filter_messages_chat_off:
//                meters = 0;
//                break;
//            case R.id.filter_messages_chat_10km:
//                meters = 10000;
//                break;
//            case R.id.filter_messages_chat_20km:
//                meters = 20000;
//                break;
//            default:
//                return;
//        }

//        setChatFiltering(mDistanceLimit);
//        resetChat();
    }

    @Override
    public void onViewCreated() {
        registerAdapter();
        mDistanceLimit = getView().getDistanceLimit();
        setChatFiltering(mDistanceLimit);
        registerChatMessagesListener();
        getView().setupAll();
    }

    @Override
    public void onDestroyView() {
        unregisterChatMessagesListener();
    }

    private void setChatFiltering(int limit) {
        mModel.filterChatToDistance(limit);
    }

    private void resetChat() {
        getView().enableSwipeLayout(true);
        mModel.unregisterChatMessagesListener();
        getView().clearMessages();
        mModel.registerChatMessagesListener();
    }

    @Override
    public void showNewMessage() {
        int position = mModel.getMessagesSize();
        getView().notifyItemInserted(position);
        if (position > 1 &&
                getView().getLastCompletelyVisibleItemPosition() == position - 2) {
            getView().scrollToPosition(mModel.getMessagesSize());
        }
    }

    @Override
    public void showOlderMessages(int startPos, int lastPos) {
        getView().notifyItemRangeInserted(startPos, lastPos);
        getView().scrollToPosition(lastPos - 1);
    }

    @Override
    public void disableRefreshingSwipeLayout() {
        getView().disableRefreshingSwipeLayout();
    }

    @Override
    public void enableSwipeLayout(boolean enable) {
        getView().enableSwipeLayout(enable);
    }

    @Override
    public void updateMessage(int position) {
        getView().updateMessage(position);
    }

    @Subscribe(sticky = true)
    public void onGpsStateChanged(GpsStatusChangedEvent event) {
        if (event.isGpsOn()) {
            getView().enableShareLocationButton();
        } else {
            getView().disableShareLocationButton();
        }
    }

    @Subscribe
    public void onShareLocationInChatAllowed(ShareLocationInChatAllowedEvent event) {
        mModel.fetchDataForLocationShare();
    }

    @Subscribe
    public void OnClickChatDialogFragment(OnClickChatDialogFragmentEvent event) {
        setChatFiltering(event.getDistanceLimit());
        resetChat();
    }
}
