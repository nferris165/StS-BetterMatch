package betterMatch.events;

import betterMatch.BetterMatch;
import betterMatch.cards.CommonCard;
import betterMatch.cards.RareCard;
import betterMatch.cards.UncommonCard;
import betterMatch.patches.customTags;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.colorless.DramaticEntrance;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractImageEvent;
import com.megacrit.cardcrawl.events.GenericEventDialog;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.EventStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;

import java.util.*;


public class BetterMatchEvent extends AbstractImageEvent {

    public static final String ID = BetterMatch.makeID("BetterMatch");
    private static final EventStrings eventStrings = CardCrawlGame.languagePack.getEventString(ID);

    private static final String NAME = eventStrings.NAME;
    private static final String[] DESCRIPTIONS = eventStrings.DESCRIPTIONS;
    private static final String[] OPTIONS = eventStrings.OPTIONS;
    private static final String IMG = "images/events/matchAndKeep.jpg";

    private AbstractCard chosenCard;
    private AbstractCard hoveredCard;
    private boolean cardFlipped = false;
    private boolean gameDone = false;
    private boolean cleanUpCalled = false;
    private boolean free;
    private int attemptCount, cost;
    private CardGroup cards;
    private float waitTimer;
    private CUR_SCREEN screen;
    private static final String MSG_2;
    private static final String MSG_3;
    private static final String GAME_MSG;

    public BetterMatchEvent() {
        super(NAME, DESCRIPTIONS[2], IMG);
        this.cards = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
        this.waitTimer = 0.0F;
        this.screen = CUR_SCREEN.INTRO;
        float roll = AbstractDungeon.eventRng.random(0.0F, 1.0F);
        this.free = roll < 0.075F;
        this.cards.group = this.initializeCards();
        Collections.shuffle(this.cards.group, new Random(AbstractDungeon.miscRng.randomLong()));
        this.imageEventText.setDialogOption(OPTIONS[0]);
        this.noCardsInRewards = true;

        //BetterMatch.logger.info(roll + "\n");

        if(AbstractDungeon.ascensionLevel >= 15){
            this.cost = 150;
        }
        else{
            this.cost = 100;
        }

        if(free){
            this.body = DESCRIPTIONS[3];
            this.cost = 0;
        }
    }

    private void stripCard(AbstractCard card){
        if(!card.hasTag(customTags.Display)) {
            switch (card.rarity) {
                case RARE:
                    card.assetUrl = BetterMatch.makeCardPath("rare.png");
                    card.portrait = null;
                    break;
                case UNCOMMON:
                    card.assetUrl = BetterMatch.makeCardPath("uncommon.png");
                    card.portrait = null;
                    break;
                case COMMON:
                    card.assetUrl = BetterMatch.makeCardPath("common.png");
                    card.portrait = null;
                    break;
            }
            card.name = "Colorless Prize!";
            card.rawDescription = "Win your choice of a colorless card";
            card.cost = -2;
            card.initializeDescription();
        }
        else{
            card.color = AbstractDungeon.player.getCardColor();
        }

    }

    private ArrayList<AbstractCard> initializeCards() {
        ArrayList<AbstractCard> retVal = new ArrayList<>();
        ArrayList<AbstractCard> retValCopy = new ArrayList<>();

        // Card Pool
        retVal.add(new RareCard());
        retVal.add(new UncommonCard());
        retVal.add(new UncommonCard());
        retVal.add(new CommonCard());
        retVal.add(new CommonCard());
        retVal.add(new DramaticEntrance());

        //retVal.add(AbstractDungeon.player.getStartCardForEvent());
        for(AbstractCard c: retVal){
            for (AbstractRelic r : AbstractDungeon.player.relics) {
                r.onPreviewObtainCard(c);
            }

            retValCopy.add(c.makeStatEquivalentCopy());
        }

        retVal.addAll(retValCopy);

        for(AbstractCard c: retVal){
            c.current_x = (float)Settings.WIDTH / 2.0F;
            c.target_x = c.current_x;
            c.current_y = -300.0F * Settings.scale;
            c.target_y = c.current_y;
            stripCard(c);
        }

        return retVal;
    }

    @Override
    public void update() {
        super.update();
        this.cards.update();
        if (this.screen == CUR_SCREEN.PLAY) {
            this.updateControllerInput();
            this.updateMatchGameLogic();
        } else if (this.screen == CUR_SCREEN.CLEAN_UP) {
            if (!this.cleanUpCalled) {
                this.cleanUpCalled = true;
                this.cleanUpCards();
            }

            if (this.waitTimer > 0.0F) {
                this.waitTimer -= Gdx.graphics.getDeltaTime();
                if (this.waitTimer < 0.0F) {
                    this.waitTimer = 0.0F;
                    this.screen = CUR_SCREEN.COMPLETE;
                    getReward();
                    GenericEventDialog.show();
                    this.imageEventText.updateBodyText(MSG_3);
                    this.imageEventText.clearRemainingOptions();
                    this.imageEventText.setDialogOption(OPTIONS[1]);
                }
            }
        }

        if (!GenericEventDialog.waitForInput) {
            this.buttonEffect(GenericEventDialog.getSelectedOption());
        }

    }

    private void updateControllerInput() {
        if (Settings.isControllerMode) {
            boolean anyHovered = false;
            int index = 0;

            for(Iterator var3 = this.cards.group.iterator(); var3.hasNext(); ++index) {
                AbstractCard c = (AbstractCard)var3.next();
                if (c.hb.hovered) {
                    anyHovered = true;
                    break;
                }
            }

            if (!anyHovered) {
                Gdx.input.setCursorPosition((int)(this.cards.group.get(0)).hb.cX, Settings.HEIGHT - (int)(this.cards.group.get(0)).hb.cY);
            } else {
                float x;
                if (!CInputActionSet.up.isJustPressed() && !CInputActionSet.altUp.isJustPressed()) {
                    if (!CInputActionSet.down.isJustPressed() && !CInputActionSet.altDown.isJustPressed()) {
                        if (!CInputActionSet.left.isJustPressed() && !CInputActionSet.altLeft.isJustPressed()) {
                            if (CInputActionSet.right.isJustPressed() || CInputActionSet.altRight.isJustPressed()) {
                                x = (this.cards.group.get(index)).hb.cX + 210.0F * Settings.scale;
                                if (x > 1375.0F * Settings.scale) {
                                    x = 640.0F * Settings.scale;
                                }

                                Gdx.input.setCursorPosition((int)x, Settings.HEIGHT - (int)(this.cards.group.get(index)).hb.cY);
                            }
                        } else {
                            x = (this.cards.group.get(index)).hb.cX - 210.0F * Settings.scale;
                            if (x < 530.0F * Settings.scale) {
                                x = 1270.0F * Settings.scale;
                            }

                            Gdx.input.setCursorPosition((int)x, Settings.HEIGHT - (int)(this.cards.group.get(index)).hb.cY);
                        }
                    } else {
                        x = (this.cards.group.get(index)).hb.cY - 230.0F * Settings.scale;
                        if (x < 175.0F * Settings.scale) {
                            x = 750.0F * Settings.scale;
                        }

                        Gdx.input.setCursorPosition((int)(this.cards.group.get(index)).hb.cX, (int)((float)Settings.HEIGHT - x));
                    }
                } else {
                    x = (this.cards.group.get(index)).hb.cY + 230.0F * Settings.scale;
                    if (x > 865.0F * Settings.scale) {
                        x = 290.0F * Settings.scale;
                    }

                    Gdx.input.setCursorPosition((int)(this.cards.group.get(index)).hb.cX, (int)((float)Settings.HEIGHT - x));
                }

                if (CInputActionSet.select.isJustPressed()) {
                    CInputActionSet.select.unpress();
                    InputHelper.justClickedLeft = true;
                }
            }

        }
    }

    private void cleanUpCards() {
        setReward(1);
        for(AbstractCard c : this.cards.group) {
            c.targetDrawScale = 0.5F;
            c.target_x = (float)Settings.WIDTH / 2.0F;
            c.target_y = -300.0F * Settings.scale;
        }
    }

    private void updateMatchGameLogic() {
        if (this.waitTimer == 0.0F) {
            this.hoveredCard = null;

            for (AbstractCard c : this.cards.group) {
                c.hb.update();
                if (this.hoveredCard == null && c.hb.hovered) {
                    c.drawScale = 0.7F;
                    c.targetDrawScale = 0.7F;
                    this.hoveredCard = c;
                    if (InputHelper.justClickedLeft && this.hoveredCard.isFlipped) {
                        InputHelper.justClickedLeft = false;
                        this.hoveredCard.isFlipped = false;
                        if (!this.cardFlipped) {
                            this.cardFlipped = true;
                            this.chosenCard = this.hoveredCard;
                        } else {
                            this.cardFlipped = false;
                            if (this.chosenCard.cardID.equals(this.hoveredCard.cardID)) {
                                this.waitTimer = 1.0F;
                                this.chosenCard.targetDrawScale = 0.7F;
                                this.chosenCard.target_x = (float) Settings.WIDTH / 2.0F;
                                this.chosenCard.target_y = (float) Settings.HEIGHT / 2.0F;
                                this.hoveredCard.targetDrawScale = 0.7F;
                                this.hoveredCard.target_x = (float) Settings.WIDTH / 2.0F;
                                this.hoveredCard.target_y = (float) Settings.HEIGHT / 2.0F;
                            } else {
                                this.waitTimer = 1.25F;
                                this.chosenCard.targetDrawScale = 1.0F;
                                this.hoveredCard.targetDrawScale = 1.0F;
                            }
                        }
                    }
                } else if (c != this.chosenCard) {
                    c.targetDrawScale = 0.5F;
                }
            }
        } else {
            this.waitTimer -= Gdx.graphics.getDeltaTime();
            if (this.waitTimer < 0.0F && !this.gameDone) {
                this.waitTimer = 0.0F;
                if (this.chosenCard.cardID.equals(this.hoveredCard.cardID)) {
                    //++this.cardsMatched;
                    this.cards.group.remove(this.chosenCard);
                    this.cards.group.remove(this.hoveredCard);
                    //AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(this.chosenCard.makeCopy(), (float)Settings.WIDTH / 2.0F, (float)Settings.HEIGHT / 2.0F));
                    this.chosenCard = null;
                    this.hoveredCard = null;
                } else {
                    this.chosenCard.isFlipped = true;
                    this.hoveredCard.isFlipped = true;
                    this.chosenCard.targetDrawScale = 0.5F;
                    this.hoveredCard.targetDrawScale = 0.5F;
                    this.chosenCard = null;
                    this.hoveredCard = null;
                }

                --this.attemptCount;
                if (this.attemptCount == 0) {
                    this.gameDone = true;
                    this.waitTimer = 1.0F;
                }
            } else if (this.gameDone) {
                this.screen = CUR_SCREEN.CLEAN_UP;
            }
        }

    }

    @Override
    protected void buttonEffect(int buttonPressed) {
        switch(this.screen) {
            case INTRO:
                switch(buttonPressed) {
                    case 0:
                        if(free){
                            this.imageEventText.updateBodyText(DESCRIPTIONS[4]);
                        } else {
                            this.imageEventText.updateBodyText(MSG_2);
                        }
                        this.imageEventText.updateDialogOption(0, OPTIONS[2] + cost + OPTIONS[4]);
                        this.imageEventText.setDialogOption(OPTIONS[2] + cost + OPTIONS[5]);
                        this.imageEventText.setDialogOption(OPTIONS[2] + cost + OPTIONS[6]);
                        this.screen = CUR_SCREEN.RULE_EXPLANATION;
                        return;
                    default:
                        return;
                }
            case RULE_EXPLANATION:
                switch(buttonPressed) {
                    case 0:
                        AbstractDungeon.player.loseGold(cost);
                        this.imageEventText.clearAllDialogs();
                        GenericEventDialog.hide();
                        this.screen = CUR_SCREEN.PLAY;
                        this.attemptCount = 5;
                        this.placeCards();
                        return;
                    case 1:
                        AbstractDungeon.player.loseGold(cost);
                        this.imageEventText.clearAllDialogs();
                        GenericEventDialog.hide();
                        this.screen = CUR_SCREEN.PLAY;
                        this.attemptCount = 3;
                        this.placeCards();
                        return;
                    case 2:
                        AbstractDungeon.player.loseGold(cost);
                        this.imageEventText.clearAllDialogs();
                        GenericEventDialog.hide();
                        this.screen = CUR_SCREEN.PLAY;
                        this.attemptCount = 1;
                        this.placeCards();
                        return;
                    default:
                        return;
                }
            case COMPLETE:
                this.openMap();
                break;
            case REWARD:
                break;
        }

    }

    private void setReward(int num){
        AbstractDungeon.getCurrRoom().rewards.clear();
        AbstractDungeon.combatRewardScreen.clear();

        for(int i = 0; i < num; i++) {
            AbstractDungeon.getCurrRoom().addCardReward(new RewardItem(AbstractCard.CardColor.COLORLESS));
        }
    }

    private void getReward() {
        AbstractDungeon.combatRewardScreen.open();
    }

    private void placeCards() {
        for(int i = 0; i < this.cards.size(); ++i) {
            (this.cards.group.get(i)).target_x = (float)(i % 4) * 210.0F * Settings.scale + 640.0F * Settings.scale;
            (this.cards.group.get(i)).target_y = (float)(i % 3) * -230.0F * Settings.scale + 750.0F * Settings.scale;
            (this.cards.group.get(i)).targetDrawScale = 0.5F;
            (this.cards.group.get(i)).isFlipped = true;
        }

    }

    @Override
    public void render(SpriteBatch sb) {
        this.cards.render(sb);
        if (this.chosenCard != null) {
            this.chosenCard.render(sb);
        }

        if (this.hoveredCard != null) {
            this.hoveredCard.render(sb);
        }

        if (this.screen == CUR_SCREEN.PLAY) {
            FontHelper.renderSmartText(sb, FontHelper.panelNameFont,
                    GAME_MSG + this.attemptCount, 780.0F * Settings.scale, 80.0F * Settings.scale,
                    2000.0F * Settings.scale, 0.0F, Color.WHITE);
        }

    }

    static {
        MSG_2 = DESCRIPTIONS[0];
        MSG_3 = DESCRIPTIONS[1];
        GAME_MSG = OPTIONS[3];
    }

    private enum CUR_SCREEN {
        INTRO,
        RULE_EXPLANATION,
        PLAY,
        COMPLETE,
        REWARD,
        CLEAN_UP;

        CUR_SCREEN() {
        }
    }
}
