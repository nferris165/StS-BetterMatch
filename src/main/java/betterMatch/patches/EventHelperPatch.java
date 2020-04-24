package betterMatch.patches;

import betterMatch.events.BetterMatchEvent;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.shrines.GremlinMatchGame;
import com.megacrit.cardcrawl.helpers.EventHelper;

public class EventHelperPatch {
    @SpirePatch(
            clz = EventHelper.class,
            method = "getEvent"
    )

    public static class EventSwapPatch {
        public static AbstractEvent Postfix(AbstractEvent __result, String key){
            //BetterStone.logger.info(AbstractDungeon.eventList + "\n\n");

            if (__result instanceof GremlinMatchGame) {

                return new BetterMatchEvent();
            }
            return __result;
        }
    }
}