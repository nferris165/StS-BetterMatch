package betterMatch.cards;

import basemod.abstracts.CustomCard;

import static com.megacrit.cardcrawl.core.CardCrawlGame.languagePack;


public abstract class AbstractCustomCard extends CustomCard {

    public String updated_desc;
    public String[] ext_desc;

    public AbstractCustomCard(final String id,
                              final String img,
                              final int cost,
                              final CardType type,
                              final CardColor color,
                              final CardRarity rarity,
                              final CardTarget target) {

        super(id, languagePack.getCardStrings(id).NAME, img, cost, languagePack.getCardStrings(id).DESCRIPTION, type, color, rarity, target);
        this.updated_desc = languagePack.getCardStrings(id).UPGRADE_DESCRIPTION;
        this.ext_desc = languagePack.getCardStrings(id).EXTENDED_DESCRIPTION;
    }
}