package org.eclipse.titanium.markers.spotters.implementation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titanium.markers.spotters.BaseProjectCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

/**
 * This class marks the following code smell:
 * There are several TTCN-3 modules with different language specification in the same project hierarchy.
 *
 * One problem with this is, that different language version have different set of keywords, support different set of features.
 * For example "private" might still be allowed as a variable name in one,
 *  while it is a keyword used to specify definition visibility in the other.
 * Such mixing requires heavy attention from developers,
 *  to know precisely all the minute differences of the language versions used in the same project,
 *  and how these differences might interact.
 *
 * This situation also indicates the problem of having aged code that was not yet updated.
 * Newer language version could report style issues and might provide additional benefits.
 *   Like being able to hide internal details from importing modules, using the "private" visibility modifier
 *
 * @author Kristof Szabados
 */
public class LanguageMixing extends BaseProjectCodeSmellSpotter {
	private static final String ERR_MSG = "Project `{0}'' contains TTCN-3 modules of several language versions: module `{1}'' is of `{2}'', while module `{3}'' is of `{4}'' ";


	public LanguageMixing() {
		super(CodeSmellType.LANGUAGE_MIXING);
	}

	@Override
	protected void process(IProject project, Problems problems) {
		TITANDebugConsole.println("Language mixing called");

		final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(project);
		final Set<String> knownModuleNames = projectSourceParser.getKnownModuleNames();
		final List<TTCN3Module> modules = new ArrayList<TTCN3Module>();
		for (final String moduleName : new TreeSet<String>(knownModuleNames)) {
			Module module = projectSourceParser.getModuleByName(moduleName);
			if(module instanceof TTCN3Module) {
				modules.add((TTCN3Module) module);
			}
		}

		Collections.sort(modules, new Comparator<TTCN3Module>() {
			@Override
			public int compare(final TTCN3Module o1, final TTCN3Module o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		String lastLanguage = null;
		TTCN3Module lastModule = null;
		for (TTCN3Module module: modules) {
			ArrayList<String> languageSpecifications = module.getLanguageSpecifictions();
			if(languageSpecifications != null && languageSpecifications.size() > 0) {
				String tempLanguage = languageSpecifications.get(0);
				if(tempLanguage != null) {
					if(lastLanguage == null) {
						lastLanguage = tempLanguage;
						lastModule = module;
					} else if (!tempLanguage.equals(lastLanguage)) {
						problems.report(module.getIdentifier().getLocation(),  MessageFormat.format(ERR_MSG, project.getName(), module.getName(), tempLanguage, lastModule.getName(), lastLanguage));
					}
				}
			}
		}
	}

}
