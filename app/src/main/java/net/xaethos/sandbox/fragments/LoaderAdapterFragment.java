package net.xaethos.sandbox.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import net.xaethos.sandbox.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LoaderAdapterFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loader_adapter, container, false);

        LoaderAdapter adapter = new LoaderAdapter(getActivity(), getLoaderManager());

        View emptyView = view.findViewById(android.R.id.empty);
        emptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
            }
        });

        ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setEmptyView(emptyView);
        listView.setAdapter(adapter);

        return view;
    }

    static class Item {
        public final int id;

        Item(int id) {
            this.id = id;
        }
    }

    static class ItemPage {
        public final Item[] items;
        public final int next;

        ItemPage(Item[] items, int next) {
            this.items = items;
            this.next = next;
        }

        ItemPage withAppendedPage(ItemPage nextPage) {
            Item[] catItems = Arrays.copyOf(items, items.length + nextPage.items.length);
            System.arraycopy(nextPage.items, 0, catItems, items.length, nextPage.items.length);
            return new ItemPage(catItems, nextPage.next);
        }
    }

    private static class ItemLoader extends AsyncTaskLoader<ItemPage> {
        final static int PAGE_SIZE = 3;
        volatile int index = 0;
        ItemPage mItems;

        public ItemLoader(Context context) {
            super(context);
        }

        @Override
        public ItemPage loadInBackground() {
            Item[] items = new Item[PAGE_SIZE];

            try {
                for (int i = 0; i < PAGE_SIZE; ++i) {
                    Log.d("XAE", "..." + (PAGE_SIZE - i));
                    items[i] = new Item(index++);
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                return null;
            }

            return new ItemPage(items, index);
        }

        /**
         * Called when there is new data to deliver to the client.  The
         * super class will take care of delivering it; the implementation
         * here just adds a little more logic.
         */
        @Override
        public void deliverResult(ItemPage items) {
            if (isReset()) {
                // An async query came in while the loader is stopped.  We
                // don't need the result.
                if (items != null) {
                    onReleaseResources(items);
                }
            }
            ItemPage oldItems = null;
            if (mItems != items) {
                oldItems = mItems;
                if (oldItems != null && items != null) {
                    items = oldItems.withAppendedPage(items);
                }
                mItems = items;
            }

            if (isStarted()) {
                // If the Loader is currently started, we can immediately
                // deliver its results.
                super.deliverResult(items);
            }

            // At this point we can release the resources associated with
            // 'oldItems' if needed; now that the new result is delivered we
            // know that it is no longer in use.
            if (oldItems != null) {
                onReleaseResources(oldItems);
            }
        }

        /**
         * Handles a request to start the Loader.
         */
        @Override
        protected void onStartLoading() {
            if (mItems != null) {
                // If we currently have a result available, deliver it
                // immediately.
                deliverResult(mItems);
            }

            if (takeContentChanged() || mItems == null) {
                // If the data has changed since the last time it was loaded
                // or is not currently available, start a load.
                forceLoad();
            }
        }

        /**
         * Handles a request to stop the Loader.
         */
        @Override
        protected void onStopLoading() {
            // Attempt to cancel the current load task if possible.
            cancelLoad();
        }

        /**
         * Handles a request to cancel a load.
         */
        @Override
        public void onCanceled(ItemPage items) {
            super.onCanceled(items);

            // At this point we can release the resources associated with 'items'
            // if needed.
            onReleaseResources(items);
        }

        /**
         * Handles a request to completely reset the Loader.
         */
        @Override
        protected void onReset() {
            super.onReset();

            // Ensure the loader is stopped
            onStopLoading();

            // At this point we can release the resources associated with 'mItems'
            // if needed.
            if (mItems != null) {
                onReleaseResources(mItems);
                mItems = null;
            }
        }

        /**
         * Helper function to take care of releasing resources associated
         * with an actively loaded data set.
         */
        protected void onReleaseResources(ItemPage items) {
            // For a simple List<> there is nothing to do.  For something
            // like a Cursor, we would close it here.
        }
    }

    private static class LoaderAdapter extends BaseAdapter
            implements LoaderManager.LoaderCallbacks<ItemPage> {

        private final Context mContext;
        private final ItemLoader mLoader;
        private List<Item> mItems;

        private LoaderAdapter(Context context, LoaderManager manager) {
            mContext = context;
            mLoader = (ItemLoader) manager.initLoader(0, null, this);
            mItems = Collections.emptyList();
        }

        @Override
        public int getCount() {
            return mItems.isEmpty() ? 0 : mItems.size() + 1;
        }

        @Override
        public Item getItem(int position) {
            if (position < mItems.size()) {
                return mItems.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            if (position < mItems.size()) {
                return getItem(position).id;
            }
            return -1;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext)
                        .inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            long itemId = getItemId(position);
            String text;
            if (itemId < 0) {
                text = "loading more...";
                mLoader.forceLoad();
            } else {
                text = "Item " + itemId;
            }

            ((TextView) convertView.findViewById(android.R.id.text1)).setText(text);
            return convertView;
        }

        @Override
        public Loader<ItemPage> onCreateLoader(int id, Bundle args) {
            return new ItemLoader(mContext);
        }

        @Override
        public void onLoadFinished(Loader<ItemPage> loader, ItemPage data) {
            mItems = Arrays.asList(data.items);
            notifyDataSetChanged();
        }

        @Override
        public void onLoaderReset(Loader<ItemPage> loader) {
            mItems = Collections.emptyList();
            notifyDataSetChanged();
        }
    }
}
