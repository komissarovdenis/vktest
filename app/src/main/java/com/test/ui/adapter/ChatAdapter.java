package com.test.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.test.app.GuiThreadExecutor;
import com.test.app.VKApplication;
import com.test.model.Chat;
import com.test.ui.util.StringUtils;
import com.test.ui.util.images.AvatarUtils;
import com.test.ui.util.images.ImageDownloader;
import com.vk.sdk.api.model.VKApiMessage;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKApiPhotoSize;
import com.vk.sdk.api.model.VKAttachments;
import com.vk.test.R;

import java.lang.ref.WeakReference;
import java.util.Date;

public class ChatAdapter extends RecyclerViewReactiveAdapter<VKApiMessage, Chat, RecyclerView.ViewHolder> {
    private static final ImageDownloader imageDownloader = VKApplication.getInstance().getImageDownloader();
    private static final Predicate<Character> largePreviews = Predicates.in(ImmutableSet.of(VKApiPhotoSize.W, VKApiPhotoSize.Z));
    private static final Function<VKApiPhotoSize, Character> getPhotoSizeType = new GetPhotoSizeType();
    private static final Predicate<VKApiMessage> isAppropriateMessage = new IsAppropriateMessage();
    private static final Predicate<VKAttachments.VKApiAttachment> isPhoto = new IsPhotoAttachment();
    private static final String LOG_TAG = "Chat adapter";
    private static final int MY_MESSAGE_FIRST_TYPE = 0;
    private static final int MY_MESSAGE_OTHER_TYPE = 1;
    private static final int OPPONENT_MESSAGE_FIRST_TYPE = 2;
    private static final int OPPONENT_MESSAGE_OTHER_TYPE = 3;

    private static final Function<Chat, ImmutableList<? extends VKApiMessage>> DATA_BUILDER =
           new Function<Chat, ImmutableList<? extends VKApiMessage>>() {
               @Override
               public ImmutableList<? extends VKApiMessage> apply(Chat input) {
                   return ImmutableList.copyOf(Iterables.filter(input.getMessages(), isAppropriateMessage));
               }
           };

    public ChatAdapter(Chat chat) {
        super(chat, DATA_BUILDER);
        setHasStableIds(true);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        switch (viewType) {
            case MY_MESSAGE_FIRST_TYPE:
                return new MessageViewHolder(
                        LayoutInflater.from(context).inflate(R.layout.view_chat_my_message_first, parent, false)
                );
            case MY_MESSAGE_OTHER_TYPE:
                return new MessageViewHolder(
                        LayoutInflater.from(context).inflate(R.layout.view_chat_my_message_other, parent, false)
                );
            case OPPONENT_MESSAGE_FIRST_TYPE:
                return new FirstOpponentMessageViewHolder(
                        LayoutInflater.from(context).inflate(R.layout.view_chat_opponent_message_first, parent, false)
                );
            case OPPONENT_MESSAGE_OTHER_TYPE:
                return new MessageViewHolder(
                        LayoutInflater.from(context).inflate(R.layout.view_chat_opponent_message_other, parent, false)
                );
            default:
                throw new IllegalArgumentException("View type is not support " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MessageViewHolder) {
            VKApiMessage message = getItem(position);
            ((MessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public int getItemViewType(int position) {
        VKApiMessage message = getItem(position);
        if (message.out) {
            return isLastMessageFromSameUser(message, position) ? MY_MESSAGE_OTHER_TYPE : MY_MESSAGE_FIRST_TYPE;
        } else {
            return isLastMessageFromSameUser(message, position) ? OPPONENT_MESSAGE_OTHER_TYPE : OPPONENT_MESSAGE_FIRST_TYPE;
        }
    }

    private boolean isLastMessageFromSameUser(VKApiMessage message, int position) {
        if (position == getItemCount() - 1) {
            return false;
        } else {
            VKApiMessage lastMessage = getItem(position + 1);
            return lastMessage.out == message.out && lastMessage.user_id == message.user_id;
        }
    }


    private static class MessageViewHolder extends RecyclerView.ViewHolder {
        private final ImageCallback imageCallback = new ImageCallback();
        private final TextView textView;
        private final TextView timeStampView;
        private final ImageView attachmentsView;
        private int attachmentId;
        protected int messageId;

        public MessageViewHolder(View itemView) {
            super(itemView);
            this.textView = (TextView) itemView.findViewById(R.id.text_view);
            this.timeStampView  = (TextView) itemView.findViewById(R.id.time_stamp_view);
            this.attachmentsView = (ImageView) itemView.findViewById(R.id.attachment_view);
        }

        public void bind(VKApiMessage message) {
            messageId = message.getId();
            textView.setText(message.body);
            textView.setVisibility(StringUtils.isBlank(message.body) ? View.GONE : View.VISIBLE);
            timeStampView.setText(getTimeStamp(timeStampView.getContext(), message.date * 1000L));
            bindAttachment(message.attachments);
        }

        private void bindAttachment(VKAttachments input) {
            // todo: show many photos from attach unfortunately have no time to implement that now
            // just show the first
            Optional<VKAttachments.VKApiAttachment> attach = Iterables.tryFind(input, isPhoto);
            attachmentsView.setVisibility(attach.isPresent() ? View.VISIBLE : View.GONE);
            if (attach.isPresent()) {
                VKAttachments.VKApiAttachment attachment = attach.get();
                if (attachment instanceof VKApiPhoto) {
                    VKApiPhoto photo = (VKApiPhoto) attachment;
                    VKApiPhotoSize preview = getPreview(photo);
                    if (preview == null) {
                        attachmentsView.setVisibility(View.GONE);
                    } else {
                        attachmentId = attachment.getId();
                        setImageViewSize(preview, attachmentsView);
                        imageCallback.bind(attachmentsView, attachmentId);

                        ListenableFuture<Bitmap> avatarFuture = imageDownloader.downloadImage(preview.src);
                        Futures.addCallback(avatarFuture, imageCallback, GuiThreadExecutor.getInstance());
                        if (!avatarFuture.isDone()) {
                            attachmentsView.setImageDrawable(
                                    attachmentsView.getResources().getDrawable(R.drawable.image_placeholder));
                        }
                    }
                }
            }
        }

        private static VKApiPhotoSize getPreview(VKApiPhoto attach) {
            ImmutableList<VKApiPhotoSize> sizes =
                    Ordering.natural().immutableSortedCopy(
                            Iterables.filter(attach.src, Predicates.not(Predicates.compose(largePreviews, getPhotoSizeType)))
                    );
            return Iterables.getLast(sizes, null);
        }

        private static void setImageViewSize(VKApiPhotoSize preview, ImageView imageView) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) imageView.getLayoutParams();
            params.height = preview.height;
            params.width = preview.width;
        }

        private static String getTimeStamp(Context context, long timeStamp) {
            return DateFormat.getTimeFormat(context).format(new Date(timeStamp));
        }

        private class ImageCallback implements FutureCallback<Bitmap> {
            private WeakReference<ImageView> imageView;
            private int attachmentId;

            public void bind(ImageView imageView, int attachmentId) {
                this.imageView = new WeakReference<>(imageView);
                this.attachmentId = attachmentId;
            }

            @Override
            public void onSuccess(Bitmap result) {
                if (attachmentId == MessageViewHolder.this.attachmentId) {
                    ImageView imageView = this.imageView.get();
                    if (imageView != null) {
                        imageView.setImageBitmap(result);
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(LOG_TAG, "Download failure " + t.getMessage());
            }
        }
    }

    private static class FirstOpponentMessageViewHolder extends MessageViewHolder {
        protected final AvatarCallback avatarCallback = new AvatarCallback();
        private final ImageView avatarView;

        public FirstOpponentMessageViewHolder(View itemView) {
            super(itemView);
            this.avatarView = (ImageView) itemView.findViewById(R.id.avatar_view);
        }

        @Override
        public void bind(VKApiMessage message) {
            super.bind(message);
            loadAvatar(AvatarUtils.getAvatar(message.user_id));
        }

        private void loadAvatar(Optional<String> photo) {
            if (photo.isPresent()) {
                avatarCallback.bind(avatarView, messageId);
                ListenableFuture<Bitmap> avatarFuture = imageDownloader.downloadImage(photo.get());
                Futures.addCallback(avatarFuture, avatarCallback, GuiThreadExecutor.getInstance());
                if (!avatarFuture.isDone()) {
                    avatarView.setImageDrawable(AvatarUtils.getPlaceholder(avatarView.getContext()));
                }
            } else {
                avatarView.setImageDrawable(AvatarUtils.getPlaceholder(avatarView.getContext()));
            }
        }

        private class AvatarCallback implements FutureCallback<Bitmap> {
            private WeakReference<ImageView> imageView;
            private int messageId;

            public void bind(ImageView imageView, int messageId) {
                this.imageView = new WeakReference<>(imageView);
                this.messageId = messageId;
            }

            @Override
            public void onSuccess(Bitmap result) {
                if (messageId == FirstOpponentMessageViewHolder.this.messageId) {
                    ImageView imageView = this.imageView.get();
                    if (imageView != null) {
                        imageView.setImageBitmap(result);
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(LOG_TAG, "Download failure " + t.getMessage());
            }
        }
    }

    private static class IsAppropriateMessage implements Predicate<VKApiMessage> {
        @Override
        public boolean apply(VKApiMessage input) {
            return !StringUtils.isBlank(input.body) || Iterables.any(input.attachments, isPhoto);
        }
    }

    private static class IsPhotoAttachment implements Predicate<VKAttachments.VKApiAttachment> {
        @Override
        public boolean apply(VKAttachments.VKApiAttachment input) {
            return VKAttachments.TYPE_PHOTO.equals(input.getType());
        }
    }

    private static class GetPhotoSizeType implements Function<VKApiPhotoSize, Character> {
        @Override
        public Character apply(VKApiPhotoSize input) {
            return input.type;
        }
    }
}
