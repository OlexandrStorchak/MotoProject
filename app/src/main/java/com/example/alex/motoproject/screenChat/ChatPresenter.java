package com.example.alex.motoproject.screenChat;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.event.ConfirmShareLocationInChatEvent;
import com.example.alex.motoproject.event.GpsStatusChangedEvent;
import com.example.alex.motoproject.event.OnClickChatDialogFragmentEvent;
import com.example.alex.motoproject.util.TextUtil;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

public class ChatPresenter implements ChatMvp.ViewToPresenter, ChatMvp.ModelToPresenter {

    private WeakReference<ChatMvp.PresenterToView> mView;
    private ChatMvp.PresenterToModel mModel;

    @Inject
    public ChatPresenter(ChatMvp.PresenterToView view) {
        mView = new WeakReference<>(view);
        mModel = new ChatModel(this);
    }

    private ChatMvp.PresenterToView getView() throws NullPointerException {
        if (mView != null) {
            return mView.get();
        } else {
            throw new NullPointerException("View is unavailable");
        }
    }

    @Override
    public void onEditTextChanged(CharSequence charSequence) {
        if (TextUtil.hasText(charSequence.toString())) {
            getView().enableSendButton();
        } else {
            getView().disableSendButton();
        }
    }

    @Override
    public void onClickSendButton(String msg) {
        mModel.sendChatMessage(TextUtil.trim(msg));
        getView().scrollToPosition(mModel.getMessagesSize());
        getView().cleanupEditText();
    }

    @Override
    public void onClickShareLocationButton() {
        EventBus.getDefault().post(new ConfirmShareLocationInChatEvent());
    }

    @Override
    public void onTouchRecyclerView() {
        getView().hideKeyboard();
    }

    private void registerChatMessagesListener() {
        mModel.registerChatMessagesListener();
    }

    private void unregisterChatMessagesListener() {
        mModel.unregisterChatMessagesListener();
    }

    private void registerAdapter() {
        getView().setAdapter(mModel.getMessages());
    }

    @Override
    public void onRefreshSwipeLayout() {
        mModel.fetchOlderChatMessages();
    }

    @Override
    public void onOptionsItemSelected(int itemId) {
        switch (itemId) {
            case R.id.filter_messages_chat:
                getView().showLocLimitDialog();
                break;
        }
    }

    @Override
    public void onViewCreated() {
        registerAdapter();
        int mDistanceLimit = getView().getDistanceLimit();
        setChatFiltering(mDistanceLimit);
        registerChatMessagesListener();
        getView().setupAll();
    }

    @Override
    public void onDestroyView() {
        unregisterChatMessagesListener();
    }

    @Override
    public void onViewAttached(ChatMvp.PresenterToView presenterToView) {
        mView = new WeakReference<>(presenterToView);
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

    @Override
    public void onNoCurrentUserLocation() {
        getView().showToast(R.string.chat_location_no_current_user_location);
    }

    @Override
    public void onGpsStateChanged(GpsStatusChangedEvent event) {
        if (event.isGpsOn()) {
            if (getView().isLocationServiceOn()) getView().showShareLocationButton();
        } else {
            getView().hideShareLocationButton();
        }
    }

    @Override
    public void onClickChatDialogFragment(OnClickChatDialogFragmentEvent event) {
        setChatFiltering(event.getDistanceLimit());
        resetChat();
    }
}
