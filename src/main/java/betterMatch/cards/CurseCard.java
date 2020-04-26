package betterMatch.cards;

import betterMatch.BetterMatch;
import betterMatch.patches.customTags;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import static betterMatch.BetterMatch.makeCardPath;

public class CurseCard extends AbstractCustomCard {

    public static final String ID = BetterMatch.makeID(CurseCard.class.getSimpleName());

    public static final String IMG = makeCardPath("curse.png");

    private static final CardRarity RARITY = CardRarity.SPECIAL;
    private static final CardTarget TARGET = CardTarget.ALL;
    private static final CardType TYPE = CardType.CURSE;
    public static final CardColor COLOR = CardColor.CURSE;

    private static final int COST = -2;

    public CurseCard() {
        super(ID, IMG, COST, TYPE, COLOR, RARITY, TARGET);

        tags.add(customTags.Display);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
    }

    @Override
    public void upgrade() {
    }
}
