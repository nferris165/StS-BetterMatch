package betterMatch.util;

import basemod.eventUtil.util.Condition;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class EventCondition implements Condition {
    @Override
    public boolean test() {

        if(AbstractDungeon.player.gold > 100){
            return true;
        }

        return false;
    }
}
