package betterMatch.cards;

import betterMatch.BetterMatch;
import betterMatch.patches.customTags;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import static betterMatch.BetterMatch.makeCardPath;

public class ColorlessUncommonCard extends AbstractCustomCard {

    public static final String ID = BetterMatch.makeID(ColorlessUncommonCard.class.getSimpleName());

    public static final String IMG = makeCardPath("uncommon.png");

    private static final CardRarity RARITY = CardRarity.UNCOMMON;
    private static final CardTarget TARGET = CardTarget.ALL;
    private static final CardType TYPE = CardType.ATTACK;
    public static final CardColor COLOR = CardColor.COLORLESS;

    private static final int COST = -2;

    public ColorlessUncommonCard() {
        super(ID, IMG, COST, TYPE, COLOR, RARITY, TARGET);

        tags.add(customTags.Display);
    }


    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
    }


    @Override
    public void upgrade() {
        if (!upgraded) {
            upgradeName();
            initializeDescription();
        }
    }
}