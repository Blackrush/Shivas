package org.shivas.data.converter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.shivas.common.maths.Range;
import org.shivas.common.statistics.CharacteristicType;

public class XMLDataOutputter implements DataOutputter {
	
	private final XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());

	@Override
	public void outputBreed(Structs.Breed breed, String fileName) throws IOException {
		Element root_elem = new Element("breeds");
		
		Element breed_elem = new Element("breed");
		breed_elem.setAttribute("id", String.valueOf(breed.id));
		breed_elem.setAttribute("startActionPoints", String.valueOf(breed.startActionPoints));
		breed_elem.setAttribute("startMovementPoints", String.valueOf(breed.startMovementPoints));
		breed_elem.setAttribute("startLife", String.valueOf(breed.startLife));
		breed_elem.setAttribute("startProspection", String.valueOf(breed.startProspection));
		
		for (Map.Entry<CharacteristicType, Map<Range, Structs.BreedLevel>> entry1 : breed.levels.entrySet()) {
			Element levels_elem = new Element("levels");
			levels_elem.setAttribute("type", entry1.getKey().name());
			
			for (Map.Entry<Range, Structs.BreedLevel> entry2 : entry1.getValue().entrySet()) {
				Element level_elem = new Element("level");
				level_elem.setAttribute("range", entry2.getKey().toString());
				level_elem.setAttribute("bonus", String.valueOf(entry2.getValue().bonus));
				level_elem.setAttribute("cost", String.valueOf(entry2.getValue().cost));
				
				levels_elem.addContent(level_elem);
			}
			breed_elem.addContent(levels_elem);
		}
		
		root_elem.addContent(breed_elem);

		out.output(root_elem, new BufferedWriter(new FileWriter(fileName + ".xml", false)));
	}

	@Override
	public void outputExperiences(Collection<Structs.Experience> exps, String fileName) throws IOException {
		Element root_elem = new Element("experiences");
		
		for (Structs.Experience exp : exps) {
			Element exp_elem = new Element("experience");
			exp_elem.setAttribute("level", String.valueOf(exp.level));
			exp_elem.setAttribute("player", String.valueOf(exp.player));
			exp_elem.setAttribute("job", String.valueOf(exp.job));
			exp_elem.setAttribute("mount", String.valueOf(exp.mount));
			exp_elem.setAttribute("alignment", String.valueOf(exp.alignment));
			
			root_elem.addContent(exp_elem);
		}
		
		out.output(root_elem, new BufferedWriter(new FileWriter(fileName + ".xml", false)));
	}

	@Override
	public void outputMaps(Collection<Structs.GameMap> maps, String fileName) throws IOException {
		Element root_elem = new Element("maps");
		
		for (Structs.GameMap map : maps) {			
			Element map_elem = new Element("map");
			map_elem.setAttribute("id", String.valueOf(map.id));
			map_elem.setAttribute("abscissa", String.valueOf(map.position.abscissa()));
			map_elem.setAttribute("ordinate", String.valueOf(map.position.ordinate()));
			map_elem.setAttribute("width", String.valueOf(map.width));
			map_elem.setAttribute("height", String.valueOf(map.height));
			map_elem.setAttribute("date", String.valueOf(map.date));
			map_elem.setAttribute("subscriber", map.subscriber ? "1" : "0");
			
			Element data_elem = new Element("data");
			data_elem.setAttribute("value", map.data);
			map_elem.addContent(data_elem);
			
			Element key_elem = new Element("key");
			key_elem.setAttribute("value", map.key);
			map_elem.addContent(key_elem);
			
			for (Structs.GameMapTrigger trigger : map.triggers) {
				Element trigger_elem = new Element("trigger");
				trigger_elem.setAttribute("id", String.valueOf(trigger.id));
				trigger_elem.setAttribute("cell", String.valueOf(trigger.cell));
				trigger_elem.setAttribute("next_map", trigger.nextMap != null ? String.valueOf(trigger.nextMap.id) : "");
				trigger_elem.setAttribute("next_cell", String.valueOf(trigger.nextCell));
				
				map_elem.addContent(trigger_elem);
			}
			
			root_elem.addContent(map_elem);
		}
		
		out.output(root_elem, new BufferedWriter(new FileWriter(fileName + ".xml", false)));
	}

}