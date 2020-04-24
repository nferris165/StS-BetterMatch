package betterMatch.events;

import betterMatch.BetterMatch;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
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
    private int attemptCount = 5;
    private CardGroup cards;
    private float waitTimer;
    private int cardsMatched;
    private CUR_SCREEN screen;
    private static final String MSG_2;
    private static final String MSG_3;
    private List<String> matchedCards;

    public BetterMatchEvent() {
        super(NAME, DESCRIPTIONS[2], IMG);
        this.cards = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
        this.waitTimer = 0.0F;
        this.cardsMatched = 0;
        this.screen = CUR_SCREEN.INTRO;
        this.cards.group = this.initializeCards();
        Collections.shuffle(this.cards.group, new Random(AbstractDungeon.miscRng.randomLong()));
        this.imageEventText.setDialogOption(OPTIONS[0]);
        this.matchedCards = new ArrayList<>();
    }

    private ArrayList<AbstractCard> initializeCards() {
        ArrayList<AbstractCard> retVal = new ArrayList<>();
        ArrayList<AbstractCard> retValCopy = new ArrayList<>();

        // Card Pool
        retVal.add(AbstractDungeon.getCard(AbstractCard.CardRarity.RARE).makeCopy());
        retVal.add(AbstractDungeon.getCard(AbstractCard.CardRarity.UNCOMMON).makeCopy());
        retVal.add(AbstractDungeon.getCard(AbstractCard.CardRarity.COMMON).makeCopy());
        retVal.add(AbstractDungeon.getCard(AbstractCard.CardRarity.UNCOMMON).makeCopy());
        retVal.add(AbstractDungeon.getCard(AbstractCard.CardRarity.COMMON).makeCopy());
        retVal.add(AbstractDungeon.returnColorlessCard().makeCopy());

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
        }

        return retVal;
    }

    public void update() {
        super.update();// 106
        this.cards.update();// 107
        if (this.screen == CUR_SCREEN.PLAY) {// 109
            this.updateControllerInput();// 110
            this.updateMatchGameLogic();// 111
        } else if (this.screen == CUR_SCREEN.CLEAN_UP) {// 112
            if (!this.cleanUpCalled) {// 113
                this.cleanUpCalled = true;// 114
                this.cleanUpCards();// 115
            }

            if (this.waitTimer > 0.0F) {// 117
                this.waitTimer -= Gdx.graphics.getDeltaTime();// 118
                if (this.waitTimer < 0.0F) {// 119
                    this.waitTimer = 0.0F;// 120
                    this.screen = CUR_SCREEN.COMPLETE;// 121
                    GenericEventDialog.show();// 122
                    this.imageEventText.updateBodyText(MSG_3);// 123
                    this.imageEventText.clearRemainingOptions();// 124
                    this.imageEventText.setDialogOption(OPTIONS[1]);// 125
                }
            }
        }

        if (!GenericEventDialog.waitForInput) {// 130
            this.buttonEffect(GenericEventDialog.getSelectedOption());// 131
        }

    }

    private void updateControllerInput() {
        if (Settings.isControllerMode) {// 136
            boolean anyHovered = false;// 140
            int index = 0;// 141

            for(Iterator var3 = this.cards.group.iterator(); var3.hasNext(); ++index) {// 142 147
                AbstractCard c = (AbstractCard)var3.next();
                if (c.hb.hovered) {// 143
                    anyHovered = true;// 144
                    break;// 145
                }
            }

            if (!anyHovered) {// 150
                Gdx.input.setCursorPosition((int)(this.cards.group.get(0)).hb.cX, Settings.HEIGHT - (int)(this.cards.group.get(0)).hb.cY);
            } else {
                float x;
                if (!CInputActionSet.up.isJustPressed() && !CInputActionSet.altUp.isJustPressed()) {// 155
                    if (!CInputActionSet.down.isJustPressed() && !CInputActionSet.altDown.isJustPressed()) {// 161
                        if (!CInputActionSet.left.isJustPressed() && !CInputActionSet.altLeft.isJustPressed()) {// 168
                            if (CInputActionSet.right.isJustPressed() || CInputActionSet.altRight.isJustPressed()) {// 174
                                x = (this.cards.group.get(index)).hb.cX + 210.0F * Settings.scale;// 175
                                if (x > 1375.0F * Settings.scale) {// 176
                                    x = 640.0F * Settings.scale;// 177
                                }

                                Gdx.input.setCursorPosition((int)x, Settings.HEIGHT - (int)(this.cards.group.get(index)).hb.cY);// 179
                            }
                        } else {
                            x = (this.cards.group.get(index)).hb.cX - 210.0F * Settings.scale;// 169
                            if (x < 530.0F * Settings.scale) {// 170
                                x = 1270.0F * Settings.scale;// 171
                            }

                            Gdx.input.setCursorPosition((int)x, Settings.HEIGHT - (int)(this.cards.group.get(index)).hb.cY);// 173
                        }
                    } else {
                        x = (this.cards.group.get(index)).hb.cY - 230.0F * Settings.scale;// 162
                        if (x < 175.0F * Settings.scale) {// 163
                            x = 750.0F * Settings.scale;// 164
                        }

                        Gdx.input.setCursorPosition((int)(this.cards.group.get(index)).hb.cX, (int)((float)Settings.HEIGHT - x));// 166
                    }
                } else {
                    x = (this.cards.group.get(index)).hb.cY + 230.0F * Settings.scale;// 156
                    if (x > 865.0F * Settings.scale) {// 157
                        x = 290.0F * Settings.scale;// 158
                    }

                    Gdx.input.setCursorPosition((int)(this.cards.group.get(index)).hb.cX, (int)((float)Settings.HEIGHT - x));// 160
                }

                if (CInputActionSet.select.isJustPressed()) {// 182
                    CInputActionSet.select.unpress();// 183
                    InputHelper.justClickedLeft = true;// 184
                }
            }

        }
    }

    private void cleanUpCards() {
        AbstractCard c;
        for(Iterator var1 = this.cards.group.iterator(); var1.hasNext(); c.target_y = -300.0F * Settings.scale) {// 190 193
            c = (AbstractCard)var1.next();
            c.targetDrawScale = 0.5F;// 191
            c.target_x = (float)Settings.WIDTH / 2.0F;// 192
        }

    }

    private void updateMatchGameLogic() {
        if (this.waitTimer == 0.0F) {// 198
            this.hoveredCard = null;// 199

            for (AbstractCard c : this.cards.group) {
                c.hb.update();// 201
                if (this.hoveredCard == null && c.hb.hovered) {// 202
                    c.drawScale = 0.7F;// 203
                    c.targetDrawScale = 0.7F;// 204
                    this.hoveredCard = c;// 205
                    if (InputHelper.justClickedLeft && this.hoveredCard.isFlipped) {// 206
                        InputHelper.justClickedLeft = false;// 207
                        this.hoveredCard.isFlipped = false;// 208
                        if (!this.cardFlipped) {// 209
                            this.cardFlipped = true;// 210
                            this.chosenCard = this.hoveredCard;// 211
                        } else {
                            this.cardFlipped = false;// 213
                            if (this.chosenCard.cardID.equals(this.hoveredCard.cardID)) {// 214
                                this.waitTimer = 1.0F;// 215
                                this.chosenCard.targetDrawScale = 0.7F;// 216
                                this.chosenCard.target_x = (float) Settings.WIDTH / 2.0F;// 217
                                this.chosenCard.target_y = (float) Settings.HEIGHT / 2.0F;// 218
                                this.hoveredCard.targetDrawScale = 0.7F;// 219
                                this.hoveredCard.target_x = (float) Settings.WIDTH / 2.0F;// 220
                                this.hoveredCard.target_y = (float) Settings.HEIGHT / 2.0F;// 221
                            } else {
                                this.waitTimer = 1.25F;// 223
                                this.chosenCard.targetDrawScale = 1.0F;// 224
                                this.hoveredCard.targetDrawScale = 1.0F;// 225
                            }
                        }
                    }
                } else if (c != this.chosenCard) {// 230
                    c.targetDrawScale = 0.5F;// 231
                }
            }
        } else {
            this.waitTimer -= Gdx.graphics.getDeltaTime();// 236
            if (this.waitTimer < 0.0F && !this.gameDone) {// 237
                this.waitTimer = 0.0F;// 238
                if (this.chosenCard.cardID.equals(this.hoveredCard.cardID)) {// 241
                    ++this.cardsMatched;// 242
                    this.cards.group.remove(this.chosenCard);// 243
                    this.cards.group.remove(this.hoveredCard);// 244
                    this.matchedCards.add(this.chosenCard.cardID);// 245
                    AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(this.chosenCard.makeCopy(), (float)Settings.WIDTH / 2.0F, (float)Settings.HEIGHT / 2.0F));// 246 247
                    this.chosenCard = null;// 248
                    this.hoveredCard = null;// 249
                } else {
                    this.chosenCard.isFlipped = true;// 251
                    this.hoveredCard.isFlipped = true;// 252
                    this.chosenCard.targetDrawScale = 0.5F;// 253
                    this.hoveredCard.targetDrawScale = 0.5F;// 254
                    this.chosenCard = null;// 255
                    this.hoveredCard = null;// 256
                }

                --this.attemptCount;// 258
                if (this.attemptCount == 0) {// 259
                    this.gameDone = true;// 260
                    this.waitTimer = 1.0F;// 261
                }
            } else if (this.gameDone) {// 263
                this.screen = CUR_SCREEN.CLEAN_UP;// 265
            }
        }

    }

    @Override
    protected void buttonEffect(int buttonPressed) {
        switch(this.screen) {// 277
            case INTRO:
                switch(buttonPressed) {// 279
                    case 0:
                        this.imageEventText.updateBodyText(MSG_2);// 281
                        this.imageEventText.updateDialogOption(0, OPTIONS[2]);// 282
                        this.screen = CUR_SCREEN.RULE_EXPLANATION;// 283
                        return;// 306
                    default:
                        return;
                }
            case RULE_EXPLANATION:
                switch(buttonPressed) {// 289
                    case 0:
                        this.imageEventText.removeDialogOption(0);// 291
                        GenericEventDialog.hide();// 292
                        this.screen = CUR_SCREEN.PLAY;// 293
                        this.placeCards();// 294
                        return;
                    default:
                        return;
                }
            case COMPLETE:
                logMetricObtainCards("Match and Keep!", this.cardsMatched + " cards matched", this.matchedCards);// 300
                this.openMap();// 301
        }

    }

    private void placeCards() {
        for(int i = 0; i < this.cards.size(); ++i) {// 309
            (this.cards.group.get(i)).target_x = (float)(i % 4) * 210.0F * Settings.scale + 640.0F * Settings.scale;// 310
            (this.cards.group.get(i)).target_y = (float)(i % 3) * -230.0F * Settings.scale + 750.0F * Settings.scale;// 311
            (this.cards.group.get(i)).targetDrawScale = 0.5F;// 312
            (this.cards.group.get(i)).isFlipped = true;// 313
        }

    }

    public void render(SpriteBatch sb) {
        this.cards.render(sb);// 319
        if (this.chosenCard != null) {// 320
            this.chosenCard.render(sb);// 321
        }

        if (this.hoveredCard != null) {// 323
            this.hoveredCard.render(sb);// 324
        }

        if (this.screen == CUR_SCREEN.PLAY) {// 327
            FontHelper.renderSmartText(sb, FontHelper.panelNameFont, OPTIONS[3] + this.attemptCount, 780.0F * Settings.scale, 80.0F * Settings.scale, 2000.0F * Settings.scale, 0.0F, Color.WHITE);// 328
        }

    }

    static {
        MSG_2 = DESCRIPTIONS[0];
        MSG_3 = DESCRIPTIONS[1];
    }

    private enum CUR_SCREEN {
        INTRO,
        RULE_EXPLANATION,
        PLAY,
        COMPLETE,
        CLEAN_UP;

        CUR_SCREEN() {
        }
    }
}
