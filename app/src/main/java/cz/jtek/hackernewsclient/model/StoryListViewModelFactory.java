package cz.jtek.hackernewsclient.model;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

/**
 * Factory for passing extra parameters to StoryListViewModel
 */
public class StoryListViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private Application mApplication;
    private String mStoryType;

    public StoryListViewModelFactory(Application application, String storyType) {
        mApplication = application;
        mStoryType = storyType;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new StoryListViewModel(mApplication, mStoryType);
    }
}
