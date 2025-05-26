package dev.crafty.core.items.serializers;

import dev.crafty.core.config.SectionWrapper;
import dev.crafty.core.config.serializer.ConfigSerializer;
import dev.crafty.core.items.ItemAction;
import dev.crafty.core.items.ItemActionCondition;

import java.util.Optional;

public class ItemActionSerializer implements ConfigSerializer<ItemAction> {
    @Override
    public void serialize(SerializationArgs<ItemAction> args) {
        ItemAction action = args.value();

        SectionWrapper section = args.section().getOrCreateSection(args.path());

        section.set("actionId", action.actionId());

        if (action.conditions() == null || action.conditions().isEmpty()) {
            section.set("conditions", null);
            return;
        }

        for (int i = 0; i < action.conditions().size(); i++) {
            ItemActionCondition condition = action.conditions().get(i);
            section.set("conditions." + i + ".expression", condition.expression());
            section.set("conditions." + i + ".denyMessage", condition.denyMessage());
        }
    }

    @Override
    public Optional<ItemAction> deserialize(SectionWrapper section, String path) {
        String actionId = section.getString(path + ".actionId").orElse(null);
        if (actionId == null) {
            return Optional.empty();
        }

        var _conditionsSection = section.getSection(path + ".conditions");

        if (_conditionsSection.isEmpty()) {
            return Optional.of(new ItemAction(actionId, null));
        }

        var conditionsSection = _conditionsSection.get();

        var conditions = conditionsSection.getKeys(false).stream()
                .map(key -> new ItemActionCondition(
                        conditionsSection.getString(key + ".expression").orElse(""),
                        conditionsSection.getString(key + ".denyMessage").orElse("")
                ))
                .toList();

        return Optional.of(new ItemAction(actionId, conditions));
    }

    @Override
    public Class<ItemAction> getType() {
        return ItemAction.class;
    }
}
