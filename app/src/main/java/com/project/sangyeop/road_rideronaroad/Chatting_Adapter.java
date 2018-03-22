package com.project.sangyeop.road_rideronaroad;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * 채팅방에서 자신의 채팅메시지는 오른쪽에,
 * 상대방의 메시지는 왼쪽에 출력해 줍니다.
 */
public class Chatting_Adapter extends BaseAdapter {

    public class ListContents {
        String msg;
        String meg_time;
        int type;
        String user_name = "기본값";
        String profileImg = null;
        int user_index;
        int type_count;

        ListContents(String _msg, int _type) {
            this.msg = _msg;
            this.type = _type;
        }

        ListContents(String _msg, int _type, String name) {
            this.msg = _msg;
            this.type = _type;
            this.user_name = name;
        }

        //        ListContents(String _msg, int _type, String name, String profileImg) {
//            this.msg = _msg;
//            this.type = _type;
//            this.user_name = name;
//            this.profileImg = profileImg;
//        }
//
        ListContents(String _msg, int _type, int index, String profileImg) {
            this.msg = _msg;
            this.type = _type;
            this.user_index = index;
            this.profileImg = profileImg;
        }

        ListContents(String _msg, int _type, int index, String profileImg, String name, int cnt) {
            this.msg = _msg;
            this.type = _type;
            this.user_index = index;
            this.profileImg = profileImg;
            this.user_name = name;
        }

        ListContents(String _msg, int _type, int index, String profileImg, String name, int cnt, String date) {
            this.msg = _msg;
            this.type = _type;
            this.user_index = index;
            this.profileImg = profileImg;
            this.user_name = name;
            this.type_count = cnt;
            this.meg_time = date;
        }


    }

    private ArrayList<ListContents> m_List;

    public Chatting_Adapter() {
        m_List = new ArrayList<ListContents>();
    }

    // 외부에서 아이템 추가 요청 시 사용
    public void add(String _msg, int _type) {

        m_List.add(new ListContents(_msg, _type));
    }

    public void add(String _msg, int _type, String name) {
        m_List.add(new ListContents(_msg, _type, name));
    }

    public void add(String _msg, int _type, int index, String imgPath) {

        m_List.add(new ListContents(_msg, _type, index, imgPath));
    }

    public void add(String _msg, int _type, int index, String imgPath, String name, int cnt) {

        m_List.add(new ListContents(_msg, _type, index, imgPath, name, cnt));
    }

    public void add(String _msg, int _type, int index, String imgPath, String name, int cnt, String date) {

        m_List.add(new ListContents(_msg, _type, index, imgPath, name, cnt, date));
    }

    // 외부에서 아이템 삭제 요청 시 사용
    public void remove(int _position) {
        m_List.remove(_position);
    }

    @Override
    public int getCount() {
        return m_List.size();
    }

    @Override
    public Object getItem(int position) {
        return m_List.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final int pos = position;
        final Context context = parent.getContext();

        TextView text = null;
        CustomHolder holder = null;
        LinearLayout layout = null;
        View viewRight = null;
        View viewLeft = null;

        ImageView profile = null;
        ImageView blank_profileImg = null;
        TextView name = null;

        TextView time_left = null;
        TextView time_right = null;

        // 리스트가 길어지면서 현재 화면에 보이지 않는 아이템은 converView가 null인 상태로 들어 옴
        if (convertView == null) {
            // view가 null일 경우 커스텀 레이아웃을 얻어 옴
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.chatting_screen_format, parent, false);

            layout = (LinearLayout) convertView.findViewById(R.id.layout);
            text = (TextView) convertView.findViewById(R.id.text);
            viewRight = (View) convertView.findViewById(R.id.imageViewright);
            viewLeft = (View) convertView.findViewById(R.id.imageViewleft);

            profile = (ImageView) convertView.findViewById(R.id.imgV_profile);
            blank_profileImg = (ImageView) convertView.findViewById(R.id.blank_profileImg);

            name = (TextView) convertView.findViewById(R.id.txtV_name);
            time_left = (TextView) convertView.findViewById(R.id.date_type_left);
            time_right = (TextView) convertView.findViewById(R.id.date_type_right);

            // 홀더 생성 및 Tag로 등록
            holder = new CustomHolder();
            holder.m_TextView = text;
            holder.layout = layout;
            holder.viewRight = viewRight;
            holder.viewLeft = viewLeft;

            holder.imgV_profile = profile;
            holder.blank_profileImg = blank_profileImg;
            holder.txtV_name = name;
            holder.msgtime_left = time_left;
            holder.msgtime_right = time_right;

            convertView.setTag(holder);
        } else {
            holder = (CustomHolder) convertView.getTag();
            text = holder.m_TextView;
            layout = holder.layout;
            viewRight = holder.viewRight;
            viewLeft = holder.viewLeft;

            profile = holder.imgV_profile;
            blank_profileImg = holder.blank_profileImg;
            name = holder.txtV_name;
            time_left = holder.msgtime_left;
            time_right = holder.msgtime_right;
        }

        // Text 등록
        text.setText(m_List.get(position).msg);

        if (m_List.get(position).type == 0) { // 딴놈이면

            time_left.setVisibility(View.GONE);
            time_right.setVisibility(View.VISIBLE);


            text.setBackgroundResource(R.drawable.chatting_bubble_left);
            layout.setGravity(Gravity.LEFT);
            viewRight.setVisibility(View.GONE);
            viewLeft.setVisibility(View.GONE);

            name.setVisibility(View.VISIBLE);
            profile.setVisibility(View.VISIBLE);
            blank_profileImg.setVisibility(View.GONE);
            time_right.setText(m_List.get(position).meg_time);

            if (m_List.get(position).type_count == 1) { // 같은 사용자가 다시말한거니까 프사와 닉넴을 출력하지 않는다.
                profile.setVisibility(View.GONE);
                blank_profileImg.setVisibility(View.VISIBLE);
                name.setVisibility(View.GONE);
            } else {
                Glide.with(context)
                        .load(m_List.get(position).profileImg)
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .error(R.drawable.profile)
                        .into(profile);
                profile.setVisibility(View.VISIBLE);
                blank_profileImg.setVisibility(View.GONE);
                name.setVisibility(View.VISIBLE);
            }


            name.setText(m_List.get(position).user_name);


        } else if (m_List.get(position).type == 1) { //나면
            time_right.setVisibility(View.GONE);
            time_left.setText(m_List.get(position).meg_time);
            time_left.setVisibility(View.VISIBLE);

            text.setBackgroundResource(R.drawable.chatting_bubble_right);
            layout.setGravity(Gravity.RIGHT);

            viewRight.setVisibility(View.GONE);
            viewLeft.setVisibility(View.GONE);

            profile.setVisibility(View.GONE);
            name.setVisibility(View.GONE);


        } else if (m_List.get(position).type == 2) {
            layout.setGravity(Gravity.CENTER);
            viewRight.setVisibility(View.VISIBLE);
            viewLeft.setVisibility(View.VISIBLE);

            profile.setVisibility(View.GONE);
            name.setVisibility(View.GONE);

        }

//        // 리스트 아이템을 터치 했을 때 이벤트 발생
//        convertView.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                // 터치 시 해당 아이템 이름 출력
//                Toast.makeText(context, "리스트 클릭 : " + m_List.get(pos), Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        // 리스트 아이템을 길게 터치 했을때 이벤트 발생
//        convertView.setOnLongClickListener(new OnLongClickListener() {
//
//            @Override
//            public boolean onLongClick(View v) {
//                // 터치 시 해당 아이템 이름 출력
//                Toast.makeText(context, "리스트 롱 클릭 : " + m_List.get(pos), Toast.LENGTH_SHORT).show();
//                return true;
//            }
//        });

        return convertView;
    }

    private class CustomHolder {
        TextView m_TextView;
        LinearLayout layout;
        View viewRight;
        View viewLeft;

        ImageView imgV_profile;
        ImageView blank_profileImg;
        TextView txtV_name;

        TextView msgtime_left;
        TextView msgtime_right;
    }
}
