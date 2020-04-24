package betterMatch.patches;

import betterMatch.BetterMatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.shrines.GremlinMatchGame;

@SpirePatch(
        clz=AbstractDungeon.class,
        method="initializeCardPools"
)
public class RemoveEventPatch {

    public static void Prefix(AbstractDungeon dungeon_instance) {
        AbstractDungeon.shrineList.remove(GremlinMatchGame.ID);

        BetterMatch.logger.info(AbstractDungeon.shrineList + "\n\n");
    }
}
