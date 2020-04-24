package betterMatch.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.shrines.GremlinMatchGame;

@SpirePatch(
        clz=AbstractDungeon.class,
        method="initializeCardPools"
)
public class RemoveEventPatch {

    public static void Prefix(AbstractDungeon dungeon_instance) {
        AbstractDungeon.eventList.remove(GremlinMatchGame.ID);

        //BetterStone.logger.info(AbstractDungeon.eventList + "\n\n");
    }
}
