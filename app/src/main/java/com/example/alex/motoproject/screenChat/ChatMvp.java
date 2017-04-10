package com.example.alex.motoproject.screenChat;

import com.example.alex.motoproject.event.GpsStatusChangedEvent;
import com.example.alex.motoproject.event.OnClickChatDialogFragmentEvent;
import com.example.alex.motoproject.event.ShareLocationInChatAllowedEvent;

import java.util.List;

public interface ChatMvp {

    interface ViewToPresenter {
        void onEditTextChanged(CharSequence charSequence);

        void onClickSendButton(String msg);

        void onClickShareLocationButton();

        void onTouchRecyclerView();

        void onRefreshSwipeLayout();

        void onOptionsItemSelected(int itemId);

        void onViewCreated();

        void onDestroyView();

        void onViewAttached(PresenterToView presenterToView);

        void onGpsStateChanged(GpsStatusChangedEvent event);

        void onShareLocationInChatAllowed(ShareLocationInChatAllowedEvent event);

        void onClickChatDialogFragment(OnClickChatDialogFragmentEvent event);
    }

    interface PresenterToView {
        void disableSendButton();

        void enableSendButton();

        void scrollToPosition(int position);

        void cleanupEditText();

        void hideKeyboard();

        void updateMessage(int position);

        void notifyItemInserted(int position);

        void notifyItemRangeInserted(int mitPos, int lastPos);

        void setAdapter(List<ChatMessage> messages);

        void disableRefreshingSwipeLayout();

        void enableSwipeLayout(boolean enable);

        void hideShareLocationButton();

        void showShareLocationButton();

        int getLastCompletelyVisibleItemPosition();

        void clearMessages();

        void showLocLimitDialog();

        int getDistanceLimit();

        void setupAll();

        void showToast(int stringId);
    }

    interface PresenterToModel {
        void sendChatMessage(String msg);

        void registerChatMessagesListener();

        void unregisterChatMessagesListener();

        int getMessagesSize();

        List<ChatMessage> getMessages();

        void fetchOlderChatMessages();

        void fetchDataForLocationShare();

        void filterChatToDistance(int meters);
    }

    interface ModelToPresenter {
        void showNewMessage();

        void showOlderMessages(int startPos, int lastPos);

        void disableRefreshingSwipeLayout();

        void enableSwipeLayout(boolean enable);

        void updateMessage(int position);

        void onNoCurrentUserLocation();
    }
}
