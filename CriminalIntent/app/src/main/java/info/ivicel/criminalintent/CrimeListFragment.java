package info.ivicel.criminalintent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by sedny on 11/09/2017.
 */

public class CrimeListFragment extends Fragment {
    private static final String TAG = "CrimeListFragment";
    private static final String SAVE_SUBTITLE_VISIBLE = "subtitle";
    
    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;
    private boolean mSubtitleVisible;
    private TextView mEmptyCrimeTextview;
    private Button mNewCrimeButton;
    
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            mSubtitleVisible = intent.getBooleanExtra(CrimePagerActivity.EXTRA_SUBTITLE_VISIBLE,
                    false);
        }
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);
    
        MenuItem subtitle = menu.findItem(R.id.show_subtitle);
        if (mSubtitleVisible) {
            subtitle.setTitle(R.string.hide_subtitle);
        } else {
            subtitle.setTitle(R.string.show_subtitle);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_crime:
                addNewCrime();
                return true;
            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime_list, container, false);
    
        mEmptyCrimeTextview = (TextView)v.findViewById(R.id.empty_crime_text);
        mNewCrimeButton = (Button)v.findViewById(R.id.new_crime_button);
    
        mCrimeRecyclerView = (RecyclerView)v.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SAVE_SUBTITLE_VISIBLE);
        }
        
        mNewCrimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewCrime();
            }
        });
        
        updateUI();
        return v;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVE_SUBTITLE_VISIBLE, mSubtitleVisible);
    }
    
    private void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(getContext());
        List<Crime> crimes = crimeLab.getCrimes();
        setIsEmptyData(crimes.isEmpty());
    
        if (mAdapter == null) {
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
        updateSubtitle();
    }
    
    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private Crime mCrime;
        private ImageView mSolvedImageView;
    
        public CrimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_crime, parent, false));
    
            mTitleTextView = (TextView)itemView.findViewById(R.id.crime_title);
            mDateTextView = (TextView)itemView.findViewById(R.id.crime_date);
            mSolvedImageView = (ImageView)itemView.findViewById(R.id.crime_solved);
    
            itemView.setOnClickListener(this);
        }
    
        @Override
        public void onClick(View v) {
            Intent intent = CrimePagerActivity.newIntent(getContext(), mCrime.getId(),
                    mSubtitleVisible);
            startActivity(intent);
        }
    
        public void bind(Crime crime) {
            mCrime = crime;
            mTitleTextView.setText(crime.getTitle());
            mDateTextView.setText(crime.getDate().toString());
            mSolvedImageView.setVisibility(mCrime.isSolved() ? View.VISIBLE : View.GONE);
        }
    }
    
    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {
        private List<Crime> mCrimes;
    
        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }
    
        @Override
        public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
    
            return new CrimeHolder(inflater, parent);
        }
    
        @Override
        public void onBindViewHolder(CrimeHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            
            holder.bind(crime);
        }
    
        @Override
        public int getItemCount() {
            return mCrimes.size();
        }
    }

    private void updateSubtitle() {
        CrimeLab crimeLab = CrimeLab.get(getContext());
        int crimeCount = crimeLab.getCrimes().size();
        String subtitle = getResources().getQuantityString(R.plurals.subtitle_format,
                crimeCount, crimeCount, crimeCount);
        if (!mSubtitleVisible) {
            subtitle = null;
        }
        ((AppCompatActivity)getActivity()).getSupportActionBar().setSubtitle(subtitle);
    }
    
    private void addNewCrime() {
        Crime crime = new Crime();
        CrimeLab crimeLab = CrimeLab.get(getContext());
        crimeLab.addCrime(crime);
        Intent intent = CrimePagerActivity.newIntent(getContext(), crime.getId(),
                mSubtitleVisible);
        startActivity(intent);
    }
    
    private void setIsEmptyData(boolean isEmpty) {
        if (isEmpty) {
            mCrimeRecyclerView.setVisibility(View.GONE);
            mEmptyCrimeTextview.setVisibility(View.VISIBLE);
            mNewCrimeButton.setVisibility(View.VISIBLE);
        } else {
            mCrimeRecyclerView.setVisibility(View.VISIBLE);
            mEmptyCrimeTextview.setVisibility(View.GONE);
            mNewCrimeButton.setVisibility(View.GONE);
        }
    }
}
