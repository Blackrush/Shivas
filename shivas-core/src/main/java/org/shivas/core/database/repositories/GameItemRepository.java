package org.shivas.core.database.repositories;

import org.atomium.EntityManager;
import org.atomium.repository.BaseEntityRepository;
import org.atomium.repository.impl.AbstractEntityRepository;
import org.atomium.util.pk.LongPrimaryKeyGenerator;
import org.atomium.util.query.Op;
import org.atomium.util.query.Query;
import org.atomium.util.query.QueryBuilder;
import org.shivas.common.collections.Collections3;
import org.shivas.data.Container;
import org.shivas.data.entity.ItemEffect;
import org.shivas.data.entity.ItemTemplate;
import org.shivas.data.entity.WeaponTemplate;
import org.shivas.protocol.client.enums.ItemPositionEnum;
import org.shivas.core.core.castables.Weapon;
import org.shivas.core.database.models.GameItem;
import org.shivas.core.database.models.Player;
import org.shivas.core.utils.Converters;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

@Singleton
public class GameItemRepository extends AbstractEntityRepository<Long, GameItem> {
	
	
	public static String effectsToString(Collection<ItemEffect> effects) {		
		return Collections3.toString(effects, ";", Converters.ITEMEFFECT_TO_STRING);
	}
	
	public static Collection<ItemEffect> stringToEffects(String string) {
		return Collections3.fromString(string, ";", Converters.STRING_TO_ITEMEFFECT);
	}
	
	private final QueryBuilder delete, persist, save;
	private final Query load;
	
	private final Container ctner;
	private final BaseEntityRepository<Integer, Player> players;

	@Inject
	public GameItemRepository(EntityManager em, Container ctner,BaseEntityRepository<Integer, Player> players) {
		super(em, new LongPrimaryKeyGenerator());
		
		this.ctner = ctner;
		this.players = players;
		
		delete = em.builder().delete("items").where("id", Op.EQ);
		
		persist = em.builder()
				.insert("items")
				.values("id", "template", "owner", "effects", "position", "quantity");
		
		save = em.builder()
				.update("items")
				.value("template").value("owner").value("effects").value("position").value("quantity")
				.where("id", Op.EQ);
		
		load = em.builder().select("items").toQuery();
	}

	@Override
	protected void onPersisted(GameItem entity) {
		super.onPersisted(entity);
		
		entity.setCloned(false);
	}

	@Override
	protected Query buildDeleteQuery(GameItem entity) {
		Query query = delete.toQuery();
		query.setParameter("id", entity.getId());
		
		return query;
	}

	@Override
	protected Query buildPersistQuery(GameItem entity) {
		Query query = persist.toQuery();
		query.setParameter("id", entity.getId());
		query.setParameter("template", entity.getTemplate().getId());
		query.setParameter("owner", entity.getOwner());
		query.setParameter("effects", effectsToString(entity.getItemEffects()));
		query.setParameter("position", entity.getPosition().value());
		query.setParameter("quantity", entity.getQuantity());
		
		return query;
	}

	@Override
	protected Query buildSaveQuery(GameItem entity) {
		Query query = save.toQuery();
		query.setParameter("id", entity.getId());
		query.setParameter("template", entity.getTemplate().getId());
		query.setParameter("owner", entity.getOwner());
		query.setParameter("effects", effectsToString(entity.getItemEffects()));
		query.setParameter("position", entity.getPosition().value());
		query.setParameter("quantity", entity.getQuantity());
		
		return query;
	}

	@Override
	protected Query buildLoadQuery() {
		return load;
	}

	@Override
	protected GameItem load(ResultSet result) throws SQLException {
        ItemTemplate tpl = ctner.get(ItemTemplate.class).byId(result.getInt("template"));

        GameItem item = tpl instanceof WeaponTemplate ? new Weapon() : new GameItem();

        item.setId(result.getLong("id"));
        item.setTemplate(tpl);
        item.setOwner(players.find(result.getInt("owner")));
        item.setItemEffects(stringToEffects(result.getString("effects")));
        item.setPosition(ItemPositionEnum.valueOf(result.getInt("position")));
        item.setQuantity(result.getInt("quantity"));

        if (item.getQuantity() > 0) {
            item.getOwner().getBag().add(item);
        }
		
		return item;
	}

	@Override
	protected void unhandledException(Exception e) {
	}

}