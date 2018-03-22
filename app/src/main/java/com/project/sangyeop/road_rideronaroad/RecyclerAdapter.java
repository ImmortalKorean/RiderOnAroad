package com.project.sangyeop.road_rideronaroad;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * Created by leesa on 2018-01-06.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private String[] titles = {"새해가 밝았습니다", "새해 복 많으세요~", "2018년에도 화이팅", "개띠분들 화이팅", "언제나 보호구 착용","더이상 쓸 말이 없습니다", "뒷바퀴는 앞바퀴를", "앞바퀴는 뒷바퀴를"};
    private String[] details = {"동해물과 백두산이 마르고 닳도록 하느님이 보우하사 우리나라만세 무궁화 삼천리", "내용", "내용", "내용", "내용", "내용." +
            "" +
            "", "내용", "내용"};
    private int[] images = {R.drawable.img1, R.drawable.img2, R.drawable.img3, R.drawable.img4, R.drawable.img5, R.drawable.img6, R.drawable.img7, R.drawable.img8};

    /**
     * 이 메서드는 ViewHolder 객체를 얻기 위해, ReclyclerView에서 호출하며, card_layout.xml파일의 뷰들을 인플레이트한다.
     * <p>
     * 그리고 ViewHolder 클래스의 인스턴스를 생성한 후, 그것을 RecyclerView에게 반환한다.
     *
     * @param viewGroup
     * @param viewType
     * @return ViewHolder 객체를 반환한다.
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_layout, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v);

        return viewHolder;

    }

    /**
     * 보여줄 데이터를 ViewHolder 객체의 뷰에 넣는 것이다.
     * <p>
     * 이 메서드에서는 ViewHolder 객체 및 화면에 보여줄 리스트 항목을 나타내는 정숫값을 인자로 받는다.
     *
     * @param viewHolder
     * @param position
     */
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        viewHolder.itemTitle.setText(titles[position]);
        viewHolder.itemDetail.setText(details[position]);
        viewHolder.itemImage.setImageResource(images[position]);

    }

    @Override
    public int getItemCount() {
        return titles.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView itemImage;
        public TextView itemTitle;
        public TextView itemDetail;

        public ViewHolder(View itemView) {
            super(itemView);
            itemImage = (ImageView) itemView.findViewById(R.id.item_image);
            itemTitle = (TextView) itemView.findViewById(R.id.item_title);
            itemDetail = (TextView) itemView.findViewById(R.id.item_detail);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int position = getAdapterPosition();

                    Snackbar.make(v, "Click detected on item : " + position, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            });
        }
    }
}
