package com.test.ui.util.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.test.model.Message;
import com.test.model.UsersLoader;
import com.test.ui.util.Optionals;
import com.test.ui.util.StringUtils;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.test.R;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.media.ThumbnailUtils.extractThumbnail;

public final class AvatarUtils {
    private static final int GAP_SIZE = 1;
    private static final int MAX_IMAGES = 4;
    private static final GetPhotoUri getPhotoUri = new GetPhotoUri();
    private static final UsersLoader usersLoader = UsersLoader.getInstance();

    private AvatarUtils() {}

    public static ImmutableSet<String> getAvatar(Message message) {
        String messagePhoto = message.getPhoto();
        if (!StringUtils.isBlank(messagePhoto)) {
            return ImmutableSet.of(messagePhoto);
        } else {
            Set<Optional<VKApiUserFull>> users = new HashSet<>();
            for (int userId : message.getChatActive()) {
                users.add(usersLoader.get(userId));
            }
            Iterable<VKApiUserFull> chatActive = Optional.presentInstances(users);
            return ImmutableSet.copyOf(
                    Iterables.limit(Optional.presentInstances(Iterables.transform(chatActive, getPhotoUri)), MAX_IMAGES)
            );
        }
    }

    public static Optional<String> getAvatar(int userId) {
        return Optionals.flatMap(usersLoader.get(userId), getPhotoUri);
    }

    public static Drawable getPlaceholder(Context context) {
        return context.getResources().getDrawable(R.drawable.avatar_placeholder);
    }

    public static Bitmap createBitmap(List<Bitmap> bitmaps, int width, int height) {
        switch (bitmaps.size()) {
            case 0:
                return null;
            case 1: {
                return bitmaps.get(0);
            }
            case 2: {
                Bitmap left = extractThumbnail(bitmaps.get(0), width / 2, height, 0);
                Bitmap right = extractThumbnail(bitmaps.get(1), width / 2, height, 0);
                return concatHorizontal(left, right, width, height);
            }
            case 3: {
                Bitmap left = extractThumbnail(bitmaps.get(0), width / 2, height, 0);
                Bitmap rightTop = extractThumbnail(bitmaps.get(1), width / 2, height / 2, 0);
                Bitmap rightBottom = extractThumbnail(bitmaps.get(2), width / 2, height / 2, 0);
                Bitmap right = concatVertical(rightTop, rightBottom, width / 2, height);
                return concatHorizontal(left, right, width, height);
            }
            case 4:
            default: {
                Bitmap leftTop = extractThumbnail(bitmaps.get(0), width / 2, height / 2, 0);
                Bitmap leftBottom = extractThumbnail(bitmaps.get(3), width / 2, height / 2, 0);
                Bitmap rightTop = extractThumbnail(bitmaps.get(1), width / 2, height / 2, 0);
                Bitmap rightBottom = extractThumbnail(bitmaps.get(2), width / 2, height / 2, 0);
                Bitmap left = concatVertical(leftTop, leftBottom, width / 2, height);
                Bitmap right = concatVertical(rightTop, rightBottom, width / 2, height);
                return concatHorizontal(left, right, width, height);
            }
        }
    }

    private static Bitmap concatVertical(Bitmap top, Bitmap bottom, int canvasWidth, int canvasHeight) {
        Bitmap bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(top, 0, -GAP_SIZE, null);
        canvas.drawBitmap(bottom, 0, canvasWidth + GAP_SIZE, null);
        top.recycle();
        bottom.recycle();
        return bitmap;
    }

    private static Bitmap concatHorizontal(Bitmap left, Bitmap right, int canvasWidth, int canvasHeight) {
        int canvasSide = Math.min(canvasWidth, canvasHeight);
        Bitmap bitmap = Bitmap.createBitmap(canvasSide, canvasSide, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(left, -GAP_SIZE, 0, null);
        canvas.drawBitmap(right, left.getWidth() + GAP_SIZE, 0, null);
        left.recycle();
        right.recycle();
        return bitmap;
    }

    private static class GetPhotoUri implements Function<VKApiUserFull, Optional<String>> {
        @Override
        public Optional<String> apply(VKApiUserFull input) {
            if (StringUtils.isBlank(input.photo_100)) {
                return Optional.absent();
            } else {
                return Optional.of(input.photo_100);
            }
        }
    }
}
