package coin2play.org.coin2playwallet.ui.settings_rates;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import coin2play.org.coin2playwallet.R;
import global.Coin2PlayRate;
import coin2play.org.coin2playwallet.ui.base.BaseRecyclerFragment;
import coin2play.org.coin2playwallet.ui.base.tools.adapter.BaseRecyclerAdapter;
import coin2play.org.coin2playwallet.ui.base.tools.adapter.BaseRecyclerViewHolder;
import coin2play.org.coin2playwallet.ui.base.tools.adapter.ListItemListeners;

/**
 * Created by akshaynexus on 7/2/17.
 */

public class RatesFragment extends BaseRecyclerFragment<Coin2PlayRate> implements ListItemListeners<Coin2PlayRate> {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        setEmptyText("No rate available");
        setEmptyTextColor(Color.parseColor("#cccccc"));
        return view;
    }

    @Override
    protected List<Coin2PlayRate> onLoading() {
        return coin2playModule.listRates();
    }

    @Override
    protected BaseRecyclerAdapter<Coin2PlayRate, ? extends Coin2PlayRateHolder> initAdapter() {
        BaseRecyclerAdapter<Coin2PlayRate, Coin2PlayRateHolder> adapter = new BaseRecyclerAdapter<Coin2PlayRate, Coin2PlayRateHolder>(getActivity()) {
            @Override
            protected Coin2PlayRateHolder createHolder(View itemView, int type) {
                return new Coin2PlayRateHolder(itemView,type);
            }

            @Override
            protected int getCardViewResource(int type) {
                return R.layout.rate_row;
            }

            @Override
            protected void bindHolder(Coin2PlayRateHolder holder, Coin2PlayRate data, int position) {
                holder.txt_name.setText(data.getCode());
                if (list.get(0).getCode().equals(data.getCode()))
                    holder.view_line.setVisibility(View.GONE);
            }
        };
        adapter.setListEventListener(this);
        return adapter;
    }

    @Override
    public void onItemClickListener(Coin2PlayRate data, int position) {
        coin2playApplication.getAppConf().setSelectedRateCoin(data.getCode());
        Toast.makeText(getActivity(),R.string.rate_selected,Toast.LENGTH_SHORT).show();
        getActivity().onBackPressed();
    }

    @Override
    public void onLongItemClickListener(Coin2PlayRate data, int position) {

    }

    private  class Coin2PlayRateHolder extends BaseRecyclerViewHolder{

        private TextView txt_name;
        private View view_line;

        protected Coin2PlayRateHolder(View itemView, int holderType) {
            super(itemView, holderType);
            txt_name = (TextView) itemView.findViewById(R.id.txt_name);
            view_line = itemView.findViewById(R.id.view_line);
        }
    }
}
