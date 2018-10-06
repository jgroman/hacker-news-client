package cz.jtek.hackernewsclient.model;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

public class ItemViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private Application mApplication;
    private long mStoryId;

    public ItemViewModelFactory(Application application, long storyId) {
        mApplication = application;
        mStoryId = storyId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ItemViewModel(mApplication, mStoryId);
    }
}
