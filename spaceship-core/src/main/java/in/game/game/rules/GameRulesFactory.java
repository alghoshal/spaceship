package in.game.game.rules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fetches appropriate rule
 * 
 * @author aghoshal
 */
public class GameRulesFactory {
	private static final Logger log = LoggerFactory.getLogger(GameRulesFactory.class);

	/**
	 * Returns the specified rule if found, or else defaults to "standard" rule
	 * 
	 * @param rule
	 * @return
	 */
	public static GameRules fetchRule(String rule) {
		if (GameRules.STANDARD.equals(rule))
			return new StandardRule();
		if (GameRules.DESPERATION.equals(rule))
			return new Desperation();
		if (GameRules.SUPER_CHARGE.equals(rule))
			return new SuperCharge();

		if (rule.endsWith(GameRules.X_SHOT)) {
			String[] ruleSplit = rule.split("-");
			Integer x = 0;
			try {
				x = Integer.parseInt(ruleSplit[0]);
			} catch (NumberFormatException nfe) {
				log.error("Invalid rule value specified not a number (defaulting to standard): " + ruleSplit[0]);
			}

			if (x <= 10 && x > 0)
				return new XShot(x);
		}

		log.error("Invalid rule: " + rule);
		return null;
	}

	/**
	 * Fetches the standard rule
	 * 
	 * @return
	 */
	public static GameRules fetchStandardRule() {
		return new StandardRule();
	}

}
