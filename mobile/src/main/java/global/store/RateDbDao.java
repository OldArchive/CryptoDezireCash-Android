package global.store;

import global.Coin2PlayRate;

/**
 * Created by akshaynexus on 3/3/18.
 */

public interface RateDbDao<T> extends AbstractDbDao<T>{

    Coin2PlayRate getRate(String coin);


    void insertOrUpdateIfExist(Coin2PlayRate coin2playRate);

}
