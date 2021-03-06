package betterMatch.cards;

import betterMatch.BetterMatch;
import betterMatch.patches.customTags;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import static betterMatch.BetterMatch.makeCardPath;

public class ColorlessRareCard extends AbstractCustomCard{

    public static final String ID = BetterMatch.makeID(ColorlessRareCard.class.getSimpleName());

    public static final String IMG = makeCardPath("rare.png");

    private static final CardRarity RARITY = CardRarity.SPECIAL;
    private static final CardTarget TARGET = CardTarget.ALL;
    private static final CardType TYPE = CardType.ATTACK;
    public static final CardColor COLOR = CardColor.COLORLESS;

    private static final int COST = -2;

    public ColorlessRareCard() {
        super(ID, IMG, COST, TYPE, COLOR, RARITY, TARGET);

        tags.add(customTags.Display);
    }

    public ColorlessRareCard(CardRarity rarity) {
        super(ID, IMG, COST, TYPE, COLOR, rarity, TARGET);

        tags.add(customTags.Display);
    }


    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
    }


    @Override
    public void upgrade() {
    }
}
