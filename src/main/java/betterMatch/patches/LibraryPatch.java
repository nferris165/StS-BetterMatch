package betterMatch.patches;

import betterMatch.cards.*;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.screens.compendium.CardLibSortHeader;
import com.megacrit.cardcrawl.screens.compendium.CardLibraryScreen;
import com.megacrit.cardcrawl.screens.mainMenu.ColorTabBar;
import javassist.CannotCompileException;
import javassist.CtBehavior;

import java.util.ArrayList;

public class LibraryPatch {
    @SpirePatch(
            clz = CardLibraryScreen.class,
            method = "sortOnOpen"
    )

    public static class RemovePatch {
        public static void Prefix(CardLibraryScreen __instance, CardGroup ___visibleCards){

            //red cards only
        }
    }

    @SpirePatch(
            clz = CardLibraryScreen.class,
            method = "didChangeTab"
    )

    public static class RemovePatch2 {
        @SpireInsertPatch
                (
                        locator = Locator.class
                )
        public static void Insert(CardLibraryScreen __instance, ColorTabBar tabBar, ColorTabBar.CurrentTab newSelection, CardGroup ___visibleCards){

            ___visibleCards.removeCard(RareCard.ID);
            ___visibleCards.removeCard(UncommonCard.ID);
            ___visibleCards.removeCard(CommonCard.ID);
            ___visibleCards.removeCard(ColorlessRareCard.ID);
            ___visibleCards.removeCard(ColorlessUncommonCard.ID);
            ___visibleCards.removeCard(CurseCard.ID);
        }
    }

    public static class Locator extends SpireInsertLocator {
        public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
            Matcher finalMatcher = new Matcher.MethodCallMatcher(CardLibSortHeader.class, "setGroup");
            return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<>(), finalMatcher);
        }
    }
}
