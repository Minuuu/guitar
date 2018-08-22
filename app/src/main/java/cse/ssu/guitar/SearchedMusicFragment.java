package cse.ssu.guitar;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import VO.DataVO;

public class SearchedMusicFragment extends Fragment {
    public static SearchedMusicFragment newInstance() {
        return new SearchedMusicFragment();
    }
    private ListView list;
    private ListViewAdapter adapter;
    private TextView trackNum;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_searched_music, container, false);

        adapter = new ListViewAdapter();
        list = (ListView)view.findViewById(R.id.searched_list);
        list.setAdapter(adapter);


        createList();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView name_view, artist_view;
                String name, artist;
                Fragment fragment = MusicFragment.newInstance();
                Bundle bundle = new Bundle();

                name_view = (TextView)view.findViewById(R.id.textView1);
                artist_view = (TextView)view.findViewById(R.id.textView2);
                name = name_view.getText().toString();
                artist = artist_view.getText().toString();

                Log.v("debug", "item selected > " + name + " : " + artist);

                DataVO data;
                data = findData(name, artist);

                Log.v("in Music List", data.toString());
                bundle.putString("data", data.toString());
                bundle.putBoolean("key", true);
                fragment.setArguments(bundle);
                replaceFragment(fragment);
            }
        });

        return view;
    }

    private void createList() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SSUGuitar/log";
        File directory = new File(path);
        File[] files = directory.listFiles();
        List<String> filesNameList = new ArrayList<>();


        if (files.length != 0) {
            for (int i = 0; i < files.length; i++) {
                filesNameList.add(files[i].getName());
            }

            Collections.sort(filesNameList, new AscendingString());
            DataVO dataVO = null;

            for (int i = 0; i < files.length; i++) {
                String fullpath = path + "/" + filesNameList.get(i);
                try {
                    FileInputStream fis = new FileInputStream(fullpath);
                    BufferedReader bufferReader = new BufferedReader(new InputStreamReader(fis));
                    String result = "", temp = "";
                    while ((temp = bufferReader.readLine()) != null) {
                        result += temp;
                    }
                    Log.v("debug", "" + result);
                    dataVO = parseData(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                adapter.addItem(ContextCompat.getDrawable(getActivity(), R.drawable.music), dataVO.getTitle(), dataVO.getArtist());
            }
        }

        trackNum = (TextView)view.findViewById(R.id.number);
        trackNum.setText(files.length + " Track");
    }

    class AscendingString implements Comparator<String> {
        @Override
        public int compare(String a, String b) {
            return b.compareTo(a);
        }
    }

    private DataVO parseData(String data) {
        DataVO result = null;
        Log.v("in parseData", data+"");
        try {
            JSONObject object = new JSONObject(data);
            result = new DataVO(object.getString("artist"), object.getString("title"), object.getString("searched_date"), object.getString("image"), object.getString("lyric"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    private DataVO findData(String title, String artist) {
        DataVO resultData = null;
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SSUGuitar/log";
        File directory = new File(path);

        if(!directory.exists()){
            directory.mkdirs();
        }

        File[] files = directory.listFiles();
        List<String> filesNameList = new ArrayList<>();
        for (int i = 0; i < files.length; i++)
            filesNameList.add(files[i].getName());
        for(int i = 0; i < files.length; i++)
            if(filesNameList.get(i).contains(title) && filesNameList.get(i).contains(artist)) {
                String realPath = path + "/" + filesNameList.get(i);
                try {
                    FileInputStream fis = new FileInputStream(realPath);
                    BufferedReader bufferReader = new BufferedReader(new InputStreamReader(fis));

                    String result="", temp="";
                    while( (temp = bufferReader.readLine()) != null ) {
                        result += temp;
                    }

                    resultData = parseData(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        return resultData;
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content, fragment).commit();
    }

}
