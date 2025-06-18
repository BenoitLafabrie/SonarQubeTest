package com.simplecity.amp_library.ui.adapters;

import android.support.annotation.Nullable;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import com.simplecityapps.recycler_adapter.adapter.CompletionListUpdateCallback;
import com.simplecityapps.recycler_adapter.adapter.ViewModelAdapter;
import com.simplecityapps.recycler_adapter.model.ViewModel;
import io.reactivex.disposables.Disposable;
import java.util.List;

public class LoggingViewModelAdapter extends ViewModelAdapter {

    private static final String TAG = "LoggingVMAdapter";

    private String instanceTag;

    public LoggingViewModelAdapter(String tag) {
        this.instanceTag = tag;
    }

    @Nullable
    @Override
    public Disposable setItems(List<ViewModel> items, @Nullable CompletionListUpdateCallback callback) {

        Crashlytics.log(Log.DEBUG, TAG, String.format("setItems called for: '%s'", instanceTag));

        return super.setItems(items, new LoggingCompletionListUpdateCallback(callback, instanceTag));
    }

    private static class LoggingCompletionListUpdateCallback implements CompletionListUpdateCallback {

        private final CompletionListUpdateCallback callback;
        private final String instanceTag;

        LoggingCompletionListUpdateCallback(@Nullable CompletionListUpdateCallback callback, String instanceTag) {
            this.callback = callback;
            this.instanceTag = instanceTag;
        }

        @Override
        public void onComplete() {
            Crashlytics.log(Log.DEBUG, TAG, String.format("setItems complete for: '%s'. Dispatching updates.", instanceTag));
            if (callback != null) {
                callback.onComplete();
            }
        }

        @Override
        public void onInserted(int position, int count) {
            if (callback != null) {
                callback.onInserted(position, count);
            }
        }

        @Override
        public void onRemoved(int position, int count) {
            if (callback != null) {
                callback.onRemoved(position, count);
            }
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            if (callback != null) {
                callback.onMoved(fromPosition, toPosition);
            }
        }

        @Override
        public void onChanged(int position, int count, Object payload) {
            if (callback != null) {
                callback.onChanged(position, count, payload);
            }
        }
    }
}
