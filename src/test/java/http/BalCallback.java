package http;

import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Created by yasara on 4/30/17.
 */
public class BalCallback implements CarbonCallback {
    public BalCallback() {
    }

    @Override public void done(CarbonMessage carbonMessage) {
        System.out.print("here I came : "+carbonMessage.getMessageBody());
    }
}
