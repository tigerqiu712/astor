package fr.inria.astor.test.repair.evaluation.operators;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import fr.inria.astor.approaches.cardumen.CardumenApproach;
import fr.inria.astor.approaches.cardumen.CardumenExhaustiveEngine4Stats;
import fr.inria.astor.approaches.jgenprog.operators.ExpressionReplaceOperator;
import fr.inria.astor.core.entities.Ingredient;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.entities.SuspiciousModificationPoint;
import fr.inria.astor.core.loop.spaces.ingredients.ingredientSearch.EfficientIngredientStrategy;
import fr.inria.astor.core.loop.spaces.ingredients.ingredientSearch.ProbabilisticIngredientStrategy;
import fr.inria.astor.core.loop.spaces.ingredients.scopes.ExpressionTypeIngredientSpace;
import fr.inria.astor.core.loop.spaces.ingredients.scopes.IngredientSpaceScope;
import fr.inria.astor.core.loop.spaces.ingredients.transformations.DynamicIngredient;
import fr.inria.astor.core.loop.spaces.ingredients.transformations.ProbabilisticTransformationStrategy;
import fr.inria.astor.core.loop.spaces.operators.AstorOperator;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.stats.Stats;
import fr.inria.astor.core.validation.validators.ProgramValidator;
import fr.inria.astor.test.repair.evaluation.regression.MathTests;
import fr.inria.astor.util.CommandSummary;
import fr.inria.main.ExecutionMode;
import fr.inria.main.evolution.AstorMain;
import spoon.reflect.code.CtCodeElement;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;

public class CardumenApproachTest {

	protected Logger log = Logger.getLogger(this.getClass().getName());

	@Test
	public void testCardumentM70() throws Exception {
		CommandSummary command = MathTests.getMath70Command();
		command.command.put("-mode", ExecutionMode.CARDUMEN.name());
		command.command.put("-flthreshold", "0.01");
		command.command.put("-maxtime", "60");
		command.command.put("-seed", "1");
		command.command.put("-maxgen", "0");
		command.command.put("-population", "1");

		AstorMain main1 = new AstorMain();
		main1.execute(command.flat());

		CardumenApproach cardumen = (CardumenApproach) main1.getEngine();

		ExpressionTypeIngredientSpace ingredientSpace = (ExpressionTypeIngredientSpace) cardumen.getIngredientStrategy()
				.getIngredientSpace();
		assertNotNull(ingredientSpace);

		ProbabilisticIngredientStrategy ingredientStrategy = (ProbabilisticIngredientStrategy) cardumen
				.getIngredientSearchStrategy();

		assertNotNull(ingredientStrategy);

		ProbabilisticTransformationStrategy probTransformation = (ProbabilisticTransformationStrategy) cardumen
				.getIngredientTransformationStrategy();
		assertNotNull(probTransformation);

		assertTrue(Boolean.valueOf(ConfigurationProperties.getProperty("cleantemplates")));

		assertEquals(1, cardumen.getOperatorSpace().size());

		assertTrue(ExpressionReplaceOperator.class.isInstance(cardumen.getOperatorSpace().getOperators().get(0)));

	}

	@Test
	public void testCardumentM70ScopeLocal() throws Exception {
		CommandSummary command = MathTests.getMath70Command();

		IngredientSpaceScope scope = IngredientSpaceScope.LOCAL;

		command.command.put("-mode", ExecutionMode.CARDUMEN.name());
		command.command.put("-flthreshold", "0.01");
		command.command.put("-maxtime", "60");
		command.command.put("-seed", "1");
		command.command.put("-maxgen", "0");
		command.command.put("-population", "1");
		command.command.put("-scope", scope.toString().toLowerCase());

		AstorMain main1 = new AstorMain();
		main1.execute(command.flat());

		CardumenApproach cardumen = (CardumenApproach) main1.getEngine();

		ExpressionTypeIngredientSpace ingredientSpace = (ExpressionTypeIngredientSpace) cardumen.getIngredientStrategy()
				.getIngredientSpace();
		assertNotNull(ingredientSpace);

		assertEquals(scope, ingredientSpace.scope);

		ProgramVariant pvar = cardumen.getVariants().get(0);

		CtElement codeElement0 = pvar.getModificationPoints().stream()
				.filter(e -> e.getCodeElement().toString().equals("solve(min, max)")).findFirst().get()
				.getCodeElement();
		List<CtCodeElement> ingredients = ingredientSpace.getIngredients(codeElement0);

		List<String> locations = ingredientSpace.getLocations();

		System.out.println("Locations " + locations);

		for (CtCodeElement ingredientElement : ingredients) {
			assertEquals(codeElement0.getParent(CtType.class).getQualifiedName(),
					ingredientElement.getParent(CtType.class).getQualifiedName());
			assertEquals(((CtExpression) codeElement0).getType().getQualifiedName(),
					((CtExpression) ingredientElement).getType().getQualifiedName());
			assertEquals("org.apache.commons.math.analysis.solvers.BisectionSolver",
					ingredientElement.getParent(CtType.class).getQualifiedName());
		}

		///
		String p1 = "org.apache.commons.math.analysis.solvers.UnivariateRealSolverImpl";
		ModificationPoint mp40 = pvar.getModificationPoints().stream()
				.filter(e -> e.getCtClass().getQualifiedName().equals(p1)).findFirst().get();
		CtElement codeElement40 = mp40.getCodeElement();
		assertEquals(p1, mp40.getCtClass().getQualifiedName());
		List<CtCodeElement> ingredients40 = ingredientSpace.getIngredients(codeElement40);

		List<?> locations40 = ingredientSpace.getLocations();

		boolean withName = false;
		for (Object location : locations40) {
			if (((MultiKey) location).getKey(0).toString()
					.contains("org.apache.commons.math.analysis.solvers.UnivariateRealSolverImp"))
				withName = true;
		}
		assertTrue(withName);

		System.out.println("Locations " + locations40);

		for (CtCodeElement ingredientElement : ingredients40) {
			assertEquals(codeElement40.getParent(CtType.class).getQualifiedName(),
					ingredientElement.getParent(CtType.class).getQualifiedName());
			assertEquals(((CtExpression) codeElement40).getType().getQualifiedName(),
					((CtExpression) ingredientElement).getType().getQualifiedName());
			assertEquals("org.apache.commons.math.analysis.solvers.UnivariateRealSolverImpl",
					ingredientElement.getParent(CtType.class).getQualifiedName());
		}

	}

	@Test
	public void testCardumentM70ScopePackage() throws Exception {
		CommandSummary command = MathTests.getMath70Command();

		IngredientSpaceScope scope = IngredientSpaceScope.PACKAGE;

		command.command.put("-mode", ExecutionMode.CARDUMEN.name());
		command.command.put("-flthreshold", "0.01");
		command.command.put("-maxtime", "60");
		command.command.put("-seed", "1");
		command.command.put("-maxgen", "0");
		command.command.put("-population", "1");
		command.command.put("-scope", scope.toString().toLowerCase());

		AstorMain main1 = new AstorMain();
		main1.execute(command.flat());

		CardumenApproach cardumen = (CardumenApproach) main1.getEngine();

		ExpressionTypeIngredientSpace ingredientSpace = (ExpressionTypeIngredientSpace) cardumen.getIngredientStrategy()
				.getIngredientSpace();
		assertNotNull(ingredientSpace);

		assertEquals(scope, ingredientSpace.scope);

		ProgramVariant pvar = cardumen.getVariants().get(0);

		CtElement codeElement0 = pvar.getModificationPoints().stream()
				.filter(e -> e.getCodeElement().toString().equals("solve(min, max)")).findFirst().get()
				.getCodeElement();

		List<CtCodeElement> ingredients = ingredientSpace.getIngredients(codeElement0);

		List<String> locations = ingredientSpace.getLocations();

		System.out.println("Locations " + locations);

		for (CtCodeElement ingredientElement : ingredients) {
			assertEquals(codeElement0.getParent(CtPackage.class).getQualifiedName(),
					ingredientElement.getParent(CtPackage.class).getQualifiedName());
			assertEquals(((CtExpression) codeElement0).getType().getQualifiedName(),
					((CtExpression) ingredientElement).getType().getQualifiedName());
			assertEquals("org.apache.commons.math.analysis.solvers",
					ingredientElement.getParent(CtPackage.class).getQualifiedName());
		}

		///

		String p1 = "org.apache.commons.math.analysis.solvers.UnivariateRealSolverImpl";
		ModificationPoint mp40 = pvar.getModificationPoints().stream()
				.filter(e -> e.getCtClass().getQualifiedName().equals(p1)).findFirst().get();
		assertEquals("org.apache.commons.math.analysis.solvers.UnivariateRealSolverImpl",
				mp40.getCtClass().getQualifiedName());
		CtElement codeElement40 = mp40.getCodeElement();

		List<CtCodeElement> ingredients40 = ingredientSpace.getIngredients(codeElement40);

		List<?> locations40 = ingredientSpace.getLocations();

		boolean withName = false;
		for (Object location : locations40) {
			if (((MultiKey) location).getKey(0).toString().equals("org.apache.commons.math.analysis.solvers"))
				withName = true;
		}
		assertTrue(withName);

		System.out.println("Locations " + locations40);

		for (CtCodeElement ingredientElement : ingredients40) {
			assertEquals(codeElement40.getParent(CtPackage.class).getQualifiedName(),
					ingredientElement.getParent(CtPackage.class).getQualifiedName());
			assertEquals(((CtExpression) codeElement40).getType().getQualifiedName(),
					((CtExpression) ingredientElement).getType().getQualifiedName());
			assertEquals("org.apache.commons.math.analysis.solvers",
					ingredientElement.getParent(CtPackage.class).getQualifiedName());
		}

	}

	@Test
	public void testCardumentM70TransformationBugSameCombination() throws Exception {
		CommandSummary command = MathTests.getMath70Command();

		IngredientSpaceScope scope = IngredientSpaceScope.PACKAGE;

		command.command.put("-mode", ExecutionMode.CARDUMEN.name());
		command.command.put("-flthreshold", "0.01");
		command.command.put("-maxtime", "60");
		command.command.put("-seed", "1");
		command.command.put("-maxgen", "0");
		command.command.put("-population", "1");
		command.command.put("-scope", scope.toString().toLowerCase());
		command.command.put("-parameters", "disablelog:true");
		int maxCombinations = 30000;
		command.command.put("-parameters", "maxVarCombination:" + maxCombinations + ":disablelog:true");

		AstorMain main1 = new AstorMain();
		main1.execute(command.flat());

		CardumenApproach cardumen = (CardumenApproach) main1.getEngine();

		ExpressionTypeIngredientSpace ingredientSpace = (ExpressionTypeIngredientSpace) cardumen.getIngredientStrategy()
				.getIngredientSpace();
		assertNotNull(ingredientSpace);

		assertEquals(scope, ingredientSpace.scope);

		ProgramVariant pvar = cardumen.getVariants().get(0);
		int mpi = 0;
		for (ModificationPoint mp : pvar.getModificationPoints()) {

			System.out.println("--mp " + (mpi) + " [" + mp.getCodeElement().getClass().getCanonicalName() + "] " + mp);
			CtElement codeElement0 = mp.getCodeElement();
			List<CtCodeElement> ingredients = ingredientSpace.getIngredients(codeElement0);
			int t = 0;
			for (CtCodeElement ctCodeElement : ingredients) {

				System.out.println("--Template-->" + ctCodeElement);

				Ingredient ingredientToModify = new Ingredient(ctCodeElement);

				List<Ingredient> ingredientsTransformed = cardumen.getIngredientTransformationStrategy().transform(mp,
						ingredientToModify);
				System.out.println(
						"--mp " + (mpi) + " [" + mp.getCodeElement().getClass().getCanonicalName() + "] " + mp);

				System.out.println("--Template-" + (t++) + " ->" + ctCodeElement);

				Set<String> ing = new HashSet<>();
				int c = 0;
				for (Ingredient ingredientInstantiated : ingredientsTransformed) {
					c++;
					if (c < 100)
						System.out.println("------Instance-->" + ingredientInstantiated.getCode());

					// TODO: Should not be duplicates
					// assertFalse(ing.contains(ingredientInstantiated.getCode().toString()));
					ing.add(ingredientInstantiated.getCode().toString());

				}
				mpi++;
			}

		}

	}

	@Test
	public void testCardumentM70TransformationBugSameCombination2() throws Exception {
		CommandSummary command = MathTests.getMath70Command();

		IngredientSpaceScope scope = IngredientSpaceScope.PACKAGE;

		command.command.put("-mode", ExecutionMode.CARDUMEN.name());
		command.command.put("-flthreshold", "0.01");
		command.command.put("-maxtime", "60");
		command.command.put("-seed", "1");
		command.command.put("-maxgen", "0");
		command.command.put("-population", "1");
		command.command.put("-scope", scope.toString().toLowerCase());
		command.command.put("-parameters", "disablelog:true");
		int maxCombinations = 30000;
		command.command.put("-parameters", "maxVarCombination:" + maxCombinations + ":disablelog:true");

		AstorMain main1 = new AstorMain();
		main1.execute(command.flat());

		CardumenApproach cardumen = (CardumenApproach) main1.getEngine();

		ExpressionTypeIngredientSpace ingredientSpace = (ExpressionTypeIngredientSpace) cardumen.getIngredientStrategy()
				.getIngredientSpace();
		assertNotNull(ingredientSpace);

		assertEquals(scope, ingredientSpace.scope);

		ProgramVariant pvar = cardumen.getVariants().get(0);
		int mpi = 0;
		ModificationPoint mp = pvar.getModificationPoints().get(0);

		System.out.println("--mp " + (mpi) + " [" + mp.getCodeElement().getClass().getCanonicalName() + "] " + mp);
		CtElement codeElement0 = mp.getCodeElement();
		List<CtCodeElement> ingredients = ingredientSpace.getIngredients(codeElement0);
		int t = 0;
		CtCodeElement ctCodeElement = ingredients.stream()
				.filter(e -> e.toString().equals("solve(_UnivariateRealFunction_0, _double_1, _double_2)")).findFirst()
				.get();

		System.out.println("--Template-->" + ctCodeElement);

		Ingredient ingredientToModify = new Ingredient(ctCodeElement);

		List<Ingredient> ingredientsTransformed = cardumen.getIngredientTransformationStrategy().transform(mp,
				ingredientToModify);
		System.out.println("--mp " + (mpi) + " [" + mp.getCodeElement().getClass().getCanonicalName() + "] " + mp);

		System.out.println("--Template-" + (t++) + " ->" + ctCodeElement);

		Set<String> ing = new HashSet<>();

		for (Ingredient ingredientInstantiated : ingredientsTransformed) {

			System.out.println("------Instance-->" + ingredientInstantiated.getCode());

			// TODO: Should not be duplicates
			// assertFalse(ing.contains(ingredientInstantiated.getCode().toString()));
			ing.add(ingredientInstantiated.getCode().toString());

		}

	}

	@Test
	public void testCardumentM70bugHEAP() throws Exception {
		CommandSummary command = MathTests.getMath70Command();

		IngredientSpaceScope scope = IngredientSpaceScope.PACKAGE;

		command.command.put("-mode", ExecutionMode.CARDUMEN.name());
		command.command.put("-flthreshold", "0.01");
		command.command.put("-maxtime", "60");
		command.command.put("-seed", "1");
		command.command.put("-maxgen", "0");
		command.command.put("-population", "1");
		command.command.put("-scope", scope.toString().toLowerCase());
		int maxCombinations = 3000;
		command.command.put("-parameters", "maxVarCombination:" + maxCombinations + ":disablelog:true");

		AstorMain main1 = new AstorMain();
		main1.execute(command.flat());

		assertEquals((Integer) maxCombinations, (Integer) ConfigurationProperties.getPropertyInt("maxVarCombination"));

		CardumenApproach cardumen = (CardumenApproach) main1.getEngine();

		ExpressionTypeIngredientSpace ingredientSpace = (ExpressionTypeIngredientSpace) cardumen.getIngredientStrategy()
				.getIngredientSpace();
		assertNotNull(ingredientSpace);

		assertEquals(scope, ingredientSpace.scope);

		ProgramVariant pvar = cardumen.getVariants().get(0);

		ModificationPoint mp = pvar.getModificationPoints().stream()
				.filter(e -> e.getCodeElement().toString().equals("i < (maximalIterationCount)")).findFirst().get();
		CtElement codeElement0 = mp.getCodeElement();
		List<CtCodeElement> ingredients = ingredientSpace.getIngredients(codeElement0);

		String template = "((_double_0 < _double_1) || (_double_0 > _double_2))";

		CtCodeElement ctCodeElement = ingredients.stream().filter(e -> e.toString().equals(template)).findFirst().get();

		System.out.println("MP code: " + codeElement0);

		System.out.println("--Template-->" + ctCodeElement);

		Ingredient ingredientToModify = new Ingredient(ctCodeElement);

		List<Ingredient> ingredientsTransformed = cardumen.getIngredientTransformationStrategy().transform(mp,
				ingredientToModify);

		// 3_13 = 2197
		assertEquals(2197, ingredientsTransformed.size());
		assertTrue(ingredientsTransformed.size() + "vs " + maxCombinations,
				ingredientsTransformed.size() <= maxCombinations);

		Set<String> ing = new HashSet<>();

		int i = 0;
		for (Ingredient ingredient : ingredientsTransformed) {
			System.out.println("--Ing-->" + (i++) + " " + ingredient);
			String transformation = ingredient.getCode().toString();
			assertFalse(ing.contains(transformation));
			ing.add(transformation);

			assertFalse("((max < min) || (min > max))".equals(transformation));
		}

	}

	@Test
	@Ignore
	public void testCardumentM70InspectIngredients() throws Exception {
		CommandSummary command = MathTests.getMath70Command();

		IngredientSpaceScope scope = IngredientSpaceScope.PACKAGE;

		command.command.put("-mode", ExecutionMode.CARDUMEN.name());
		command.command.put("-flthreshold", "0.01");
		command.command.put("-maxtime", "60");
		command.command.put("-seed", "1");
		command.command.put("-maxgen", "0");
		command.command.put("-population", "1");
		command.command.put("-scope", scope.toString().toLowerCase());

		AstorMain main1 = new AstorMain();
		main1.execute(command.flat());

		CardumenApproach cardumen = (CardumenApproach) main1.getEngine();

		ExpressionTypeIngredientSpace ingredientSpace = (ExpressionTypeIngredientSpace) cardumen.getIngredientStrategy()
				.getIngredientSpace();
		assertNotNull(ingredientSpace);

		assertEquals(scope, ingredientSpace.scope);

		ProgramVariant pvar = cardumen.getVariants().get(0);
		int mpi = 0;
		ModificationPoint mp = pvar.getModificationPoints().get(245);

		System.out.println("--mp " + (mpi) + " [" + mp.getCodeElement().getClass().getCanonicalName() + "] " + mp);
		CtElement codeElement0 = mp.getCodeElement();
		List<CtCodeElement> ingredients = ingredientSpace.getIngredients(codeElement0);

		CtCodeElement ctCodeElement = ingredients.get(23);

		System.out.println("--Template-->" + ctCodeElement);

		Ingredient ingredientToModify = new Ingredient(ctCodeElement);

		List<Ingredient> ingredientsTransformed = cardumen.getIngredientTransformationStrategy().transform(mp,
				ingredientToModify);
		System.out.println("--mp " + (mpi) + " [" + mp.getCodeElement().getClass().getCanonicalName() + "] " + mp);

		int c = 0;
		for (Ingredient ingredientInstantiated : ingredientsTransformed) {
			c++;
			if (c < 100)
				System.out.println("------Instance-->" + ingredientInstantiated.getCode());

		}

	}

	@Test
	public void testCardumentM70IngredientPartialMapped() throws Exception {
		CommandSummary command = MathTests.getMath70Command();

		IngredientSpaceScope scope = IngredientSpaceScope.PACKAGE;

		command.command.put("-mode", ExecutionMode.CARDUMEN.name());
		command.command.put("-flthreshold", "0.01");
		command.command.put("-maxtime", "60");
		command.command.put("-seed", "1");
		command.command.put("-maxgen", "0");
		command.command.put("-population", "1");
		command.command.put("-scope", scope.toString().toLowerCase());

		AstorMain main1 = new AstorMain();
		main1.execute(command.flat());

		CardumenApproach cardumen = (CardumenApproach) main1.getEngine();

		ExpressionTypeIngredientSpace ingredientSpace = (ExpressionTypeIngredientSpace) cardumen.getIngredientStrategy()
				.getIngredientSpace();
		assertNotNull(ingredientSpace);

		assertEquals(scope, ingredientSpace.scope);

		ProgramVariant pvar = cardumen.getVariants().get(0);

		ModificationPoint mp15 = pvar.getModificationPoints().stream()
				.filter(e -> e.getCodeElement().toString().equals("(a + b) * 0.5")).findFirst().get();

		assertNotNull(mp15);
		System.out.println("-->" + mp15.getCodeElement());
		List<CtCodeElement> ingredients = ingredientSpace.getIngredients(mp15.getCodeElement());

		String code = "solve(_UnivariateRealFunction_0, _double_1, _double_2)";

		CtCodeElement template = ingredients.stream().filter(e -> e.toString().equals(code)).findFirst().get();
		Ingredient ingredientToModify = new Ingredient(template);
		assertNotNull(ingredientToModify);
		assertEquals(code, template.toString());

		List<Ingredient> ingredientsTransformed = cardumen.getIngredientTransformationStrategy().transform(mp15,
				ingredientToModify);
		System.out.println("MPoint: " + mp15);

		assertTrue(ingredientsTransformed.isEmpty());

		///
		System.out.println("Checking ");
		ModificationPoint mp0 = pvar.getModificationPoints().stream()
				.filter(e -> e.getCodeElement().toString().equals("solve(min, max)")).findFirst().get();
		assertEquals("solve(min, max)", mp0.getCodeElement().toString());

		List<CtCodeElement> ingredients0 = ingredientSpace.getIngredients(mp0.getCodeElement());

		CtCodeElement element = ingredients0.stream().filter(e -> code.equals(e.toString())).findFirst().get();
		Ingredient ingredientToModify0 = new Ingredient(element);
		assertEquals(code, element.toString());

		List<Ingredient> ingredientsTransformed0 = cardumen.getIngredientTransformationStrategy().transform(mp0,
				ingredientToModify0);
		System.out.println("MPoint: " + mp15);

		boolean found = false;
		assertTrue(!ingredientsTransformed0.isEmpty());
		for (Ingredient ingredient : ingredientsTransformed0) {
			System.out.println(ingredient.getCode());
			if (ingredient.getCode().toString().equals("solve(f, min, max)")) {
				found = true;
			}
		}
		assertTrue(found);
	}

	@Test
	@Ignore
	@Deprecated // since we dont manage the VarAccess
	public void testCardumentM70TransformationReference() throws Exception {
		CommandSummary command = MathTests.getMath70Command();

		IngredientSpaceScope scope = IngredientSpaceScope.PACKAGE;

		command.command.put("-mode", ExecutionMode.CARDUMEN.name());
		command.command.put("-flthreshold", "0.01");
		command.command.put("-maxtime", "60");
		command.command.put("-seed", "1");
		command.command.put("-maxgen", "0");
		command.command.put("-population", "1");
		command.command.put("-scope", scope.toString().toLowerCase());

		AstorMain main1 = new AstorMain();
		main1.execute(command.flat());

		CardumenApproach cardumen = (CardumenApproach) main1.getEngine();

		ExpressionTypeIngredientSpace ingredientSpace = (ExpressionTypeIngredientSpace) cardumen.getIngredientStrategy()
				.getIngredientSpace();
		assertNotNull(ingredientSpace);

		assertEquals(scope, ingredientSpace.scope);

		ProgramVariant pvar = cardumen.getVariants().get(0);

		System.out.println("Checking ");
		ModificationPoint mp15 = pvar.getModificationPoints().get(15);

		assertEquals("lower >= upper", mp15.getCodeElement().toString());

		List<CtCodeElement> ingredients = ingredientSpace.getIngredients(mp15.getCodeElement());
		System.out.println(ingredients);
		assertEquals("_double_0", ingredients.get(1).toString());

		Ingredient ingredientToModify = new Ingredient(ingredients.get(1));

		List<Ingredient> ingredientsTransformed = cardumen.getIngredientTransformationStrategy().transform(mp15,
				ingredientToModify);

		for (Ingredient ingInstance : ingredientsTransformed) {
			assertTrue(DynamicIngredient.class.isInstance(ingInstance));
			System.out.println(ingInstance.getCode());
			String c = ingInstance.getCode().toString();
			assertFalse("_double_0".equals(c));
		}

	}

	@Test
	public void testCardumentM70Evolve() throws Exception {
		CommandSummary command = MathTests.getMath70Command();

		IngredientSpaceScope scope = IngredientSpaceScope.PACKAGE;

		command.command.put("-mode", ExecutionMode.CARDUMEN.name());
		command.command.put("-flthreshold", "0.01");
		command.command.put("-maxtime", "60");
		command.command.put("-seed", "400");
		command.command.put("-maxgen", "200");
		command.command.put("-population", "1");
		command.command.put("-scope", scope.toString().toLowerCase());
		command.command.put("-parameters", "disablelog:false");
		command.command.put("-maxVarCombination", "100");

		AstorMain main1 = new AstorMain();
		main1.execute(command.flat());

		CardumenApproach cardumen = (CardumenApproach) main1.getEngine();

		ExpressionTypeIngredientSpace ingredientSpace = (ExpressionTypeIngredientSpace) cardumen.getIngredientStrategy()
				.getIngredientSpace();
		assertNotNull(ingredientSpace);

		assertTrue(cardumen.getSolutions().size() > 0);
	}

	@Test
	public void testCardumentM70EvolveNotUniformreplacement() throws Exception {
		CommandSummary command = MathTests.getMath70Command();

		IngredientSpaceScope scope = IngredientSpaceScope.PACKAGE;
		// Configuration for paper experiment
		command.command.put("-mode", ExecutionMode.CARDUMEN.name());
		command.command.put("-flthreshold", "0.1");
		command.command.put("-maxtime", "60");
		command.command.put("-population", "1");
		command.command.put("-maxgen", "100");
		command.command.put("-scope", scope.toString().toLowerCase());
		command.command.put("-parameters",
				"limitbysuspicious:false:" + "disablelog:true:uniformreplacement:false:frequenttemplate:true");
		command.command.put("-loglevel", Level.INFO.toString());
		command.command.put("-maxVarCombination", "1000");
		command.command.put("-stopfirst", "false");
		AstorMain main1 = new AstorMain();
		main1.execute(command.flat());

		CardumenApproach cardumen = (CardumenApproach) main1.getEngine();

		ExpressionTypeIngredientSpace ingredientSpace = (ExpressionTypeIngredientSpace) cardumen.getIngredientStrategy()
				.getIngredientSpace();
		assertNotNull(ingredientSpace);

	}

	@Test
	public void testCardumentM70EvolveUniformreplacement() throws Exception {
		CommandSummary command = MathTests.getMath70Command();

		IngredientSpaceScope scope = IngredientSpaceScope.LOCAL;

		command.command.put("-mode", ExecutionMode.CARDUMEN.name());
		command.command.put("-flthreshold", "0.01");
		command.command.put("-maxtime", "60");
		command.command.put("-seed", "400");
		command.command.put("-maxgen", "200");
		command.command.put("-population", "1");
		command.command.put("-scope", scope.toString().toLowerCase());
		command.command.put("-parameters", "disablelog:true:uniformreplacement:true");

		AstorMain main1 = new AstorMain();
		main1.execute(command.flat());

		CardumenApproach cardumen = (CardumenApproach) main1.getEngine();

		ExpressionTypeIngredientSpace ingredientSpace = (ExpressionTypeIngredientSpace) cardumen.getIngredientStrategy()
				.getIngredientSpace();
		assertNotNull(ingredientSpace);

		for (Object keys : ingredientSpace.mkp.keySet()) {
			System.out.println(keys + ": ");
			System.out.println(ingredientSpace.mkp.get(keys));
		}

		assertTrue(cardumen.getSolutions().size() > 0);

	}

	@Test
	public void testCardumentM70EvolveUniformreplacementMostUsedTemplate() throws Exception {
		CommandSummary command = MathTests.getMath70Command();

		IngredientSpaceScope scope = IngredientSpaceScope.LOCAL;

		command.command.put("-mode", ExecutionMode.CARDUMEN.name());
		command.command.put("-flthreshold", "0.01");
		command.command.put("-maxtime", "60");
		command.command.put("-seed", "400");
		command.command.put("-maxgen", "200");
		command.command.put("-population", "1");
		command.command.put("-scope", scope.toString().toLowerCase());
		command.command.put("-stopfirst", "false");
		command.command.put("-parameters", "disablelog:true:uniformreplacement:true:frequenttemplate:true");

		AstorMain main1 = new AstorMain();
		main1.execute(command.flat());

		CardumenApproach cardumen = (CardumenApproach) main1.getEngine();

		ExpressionTypeIngredientSpace ingredientSpace = (ExpressionTypeIngredientSpace) cardumen.getIngredientStrategy()
				.getIngredientSpace();
		assertNotNull(ingredientSpace);

		assertTrue(cardumen.getSolutions().size() > 0);

	}

	@Test
	public void testCardumentM70Suspicious() throws Exception {
		CommandSummary command = MathTests.getMath70Command();

		IngredientSpaceScope scope = IngredientSpaceScope.LOCAL;

		command.command.put("-mode", ExecutionMode.CARDUMEN.name());
		Integer maxsusp = 2;
		command.command.put("-maxsuspcandidates", maxsusp.toString());

		command.command.put("-flthreshold", "0.1");
		command.command.put("-maxtime", "60");
		command.command.put("-seed", "400");
		command.command.put("-maxgen", "0");
		command.command.put("-population", "1");
		command.command.put("-scope", scope.toString().toLowerCase());
		command.command.put("-stopfirst", "false");
		command.command.put("-parameters", "disablelog:true:uniformreplacement:true:frequenttemplate:true");

		AstorMain main1 = new AstorMain();
		main1.execute(command.flat());

		CardumenApproach cardumen = (CardumenApproach) main1.getEngine();

		assertEquals((int) maxsusp, cardumen.getVariants().get(0).getModificationPoints().size());

		main1 = new AstorMain();
		maxsusp = 100;
		command.command.put("-maxsuspcandidates", maxsusp.toString());
		command.command.put("-flthreshold", "0.9");

		main1.execute(command.flat());

		cardumen = (CardumenApproach) main1.getEngine();

		assertEquals(1, cardumen.getVariants().get(0).getModificationPoints().size());

		//

		main1 = new AstorMain();
		maxsusp = 100;
		command.command.put("-maxsuspcandidates", maxsusp.toString());
		command.command.put("-flthreshold", "0.000000001");
		command.command.put("-parameters", "limitbysuspicious:false");

		main1.execute(command.flat());

		cardumen = (CardumenApproach) main1.getEngine();

		assertTrue(cardumen.getVariants().get(0).getModificationPoints().size() > 50);

	}

	@Test
	public void testCardumentM70AssertStats() throws Exception {
		CommandSummary command = MathTests.getMath70Command();

		IngredientSpaceScope scope = IngredientSpaceScope.LOCAL;

		command.command.put("-mode", ExecutionMode.CARDUMEN.name());
		command.command.put("-flthreshold", "0.1");
		command.command.put("-maxtime", "60");
		command.command.put("-seed", "400");
		command.command.put("-maxgen", "0");
		command.command.put("-population", "1");
		command.command.put("-scope", scope.toString().toLowerCase());
		command.command.put("-stopfirst", "false");
		command.command.put("-loglevel", "DEBUG");
		command.command.put("-parameters", "disablelog:true:uniformreplacement:true:frequenttemplate:true");

		AstorMain main1 = new AstorMain();
		main1.execute(command.flat());
		Stats.createStat();
		CardumenApproach cardumen = (CardumenApproach) main1.getEngine();

		EfficientIngredientStrategy estrategy = (EfficientIngredientStrategy) cardumen.getIngredientStrategy();
		ExpressionTypeIngredientSpace ingredientSpace = (ExpressionTypeIngredientSpace) cardumen.getIngredientStrategy()
				.getIngredientSpace();
		assertNotNull(ingredientSpace);

		assertEquals(0, Stats.createStat().combinationByIngredientSize.size());
		assertEquals(0, Stats.createStat().ingredientSpaceSize.size());

		ModificationPoint mp1 = cardumen.getVariants().get(0).getModificationPoints().get(0);

		AstorOperator op1 = cardumen.getOperatorSpace().getOperators().get(0);
		List elements = estrategy.getNotExhaustedBaseElements(mp1, op1);
		int initialIngredients = elements.size();
		assertEquals(elements.size(), ingredientSpace.getIngredients(mp1.getCodeElement()).size());

		assertTrue(elements.size() > 0);

		// assertEquals(0, Stats.createStat().ingredientSpaceSize.get());

		List<CtCodeElement> bases1 = estrategy.getNotExhaustedBaseElements(mp1, op1);

		assertNotNull(bases1);

		CtCodeElement base1 = bases1.get(0);
		assertNotNull(base1);
		System.out.println("base 1:" + base1);

		// assertEquals(1, (int)
		// Stats.getCurrentStats().ingredientSpaceSize.get(bases.size()));

		List<Ingredient> ingredientsAfterTransformation = estrategy.getInstancesFromBase(mp1, op1,
				new Ingredient(base1));
		int conmbination1Size = ingredientsAfterTransformation.size();
		assertTrue(conmbination1Size > 0);

		long nrcomb = ingredientsAfterTransformation.size();
		Ingredient ins1 = estrategy.getNotUsedTransformedElement(mp1, op1, new Ingredient(base1));

		assertNotNull(ins1);
		System.out.println(Stats.currentStat.combinationByIngredientSize);

		assertTrue(Stats.currentStat.combinationByIngredientSize.containsKey(nrcomb));
		// Two attempts done before
		assertEquals(2, (long) Stats.currentStat.combinationByIngredientSize.get(nrcomb));

		for (int i = 1; i < nrcomb; i++) {
			System.out.println("-->" + i);
			Ingredient ins1i = estrategy.getNotUsedTransformedElement(mp1, op1, new Ingredient(base1));
			assertNotNull(ins1i);

			assertEquals(i + 1, Stats.currentStat.combinationByIngredientSize.keySet().size());
			System.out.println(Stats.currentStat.combinationByIngredientSize);
			assertTrue(Stats.currentStat.combinationByIngredientSize.containsKey(nrcomb - i));
			assertEquals(1, (long) Stats.currentStat.combinationByIngredientSize.get(nrcomb - i));
			if (Stats.currentStat.combinationByIngredientSize.containsKey((long) 0)) {
				System.out.println("Putting zero for " + i);
			}
			if (i != 120)
				assertNull(estrategy.exhaustTemplates.get(estrategy.getKey(mp1, op1)));
		}
		for (Long i : Stats.currentStat.combinationByIngredientSize.keySet()) {
			assertTrue("--" + i, i <= 121 && i > 0);
		}
		assertEquals(nrcomb, Stats.currentStat.combinationByIngredientSize.keySet().size());
		assertFalse(Stats.currentStat.combinationByIngredientSize.containsKey((long) 0));

		// Cloning stats to be sure that is not modified when there is no more
		// ingredients to select
		Map clonedCombStats = new HashMap<>(Stats.currentStat.combinationByIngredientSize);
		// Any ingredients to add....
		Ingredient ins1i = estrategy.getNotUsedTransformedElement(mp1, op1, new Ingredient(base1));
		assertNull(ins1i);
		// Now, after the space is completely navigated, a zero must Not be
		// written
		assertFalse(Stats.currentStat.combinationByIngredientSize.containsKey((long) 0));

		// The same that before, the cache of ingredients would return zero
		// ingredients
		Ingredient ins1i2 = estrategy.getNotUsedTransformedElement(mp1, op1, new Ingredient(base1));
		assertNull(ins1i2);

		// No change in the stats
		assertEquals(clonedCombStats.keySet().size(), Stats.currentStat.combinationByIngredientSize.keySet().size());

		for (Long i : Stats.currentStat.combinationByIngredientSize.keySet()) {
			assertTrue("--" + i, i <= 121 && i > 0);
			assertEquals(clonedCombStats.get(i), Stats.currentStat.combinationByIngredientSize.get(i));
		}

		for (int i = 1; i < conmbination1Size; i++) {
			assertEquals("-->" + i, 1, Stats.currentStat.combinationByIngredientSize.get((long) i), 0);

		}

		assertEquals(nrcomb, Stats.currentStat.combinationByIngredientSize.keySet().size());

		assertEquals(1, estrategy.exhaustTemplates.keySet().size());
		assertTrue(!estrategy.exhaustTemplates.get(estrategy.getKey(mp1, op1)).isEmpty());
		assertTrue(estrategy.exhaustTemplates.get(estrategy.getKey(mp1, op1)).contains(base1));

		System.out.println("-------Base 2-------------");

		CtCodeElement base2 = bases1.get(1);
		assertNotNull(base2);
		System.out.println("base 2" + base2);
		assertEquals(1, estrategy.exhaustTemplates.keySet().size());

		List<CtCodeElement> bases2 = estrategy.getNotExhaustedBaseElements(mp1, op1);

		assertNotNull(bases2);

		List<Ingredient> ingredientsAfterTransformationNull = estrategy.getInstancesFromBase(mp1, op1,
				new Ingredient(base1));
		assertNull(ingredientsAfterTransformationNull);

		List<Ingredient> ingredientsAfterTransformation2 = estrategy.getInstancesFromBase(mp1, op1,
				new Ingredient(base2));

		int conmbination2 = ingredientsAfterTransformation2.size();
		assertTrue(conmbination2 > 0);
		int nrcomb2 = ingredientsAfterTransformation2.size();
		assertEquals(bases1.size(), bases2.size() + 1);

		assertFalse(bases2.contains(base1));

		System.out.println("-------Base 3-----------");
		List<CtCodeElement> bases3 = estrategy.getNotExhaustedBaseElements(mp1, op1);

		System.out.println("\nbase 3 " + bases3);
		CtCodeElement base3 = bases3.get(2);
		System.out.println("before 3: " + Stats.currentStat.combinationByIngredientSize);

		List<Ingredient> ingredientsAfterTransformation3 = estrategy.getInstancesFromBase(mp1, op1,
				new Ingredient(base3));
		System.out.println("Ingredients for base 3\n" + ingredientsAfterTransformation3.size());
		assertTrue(ingredientsAfterTransformation3.size() > 0);

		int ingredientsAfterTransformation3Size = ingredientsAfterTransformation3.size();
		for (int i = 1; i <= ingredientsAfterTransformation3Size; i++) {
			System.out.println("-->" + i);
			Ingredient ins3i = estrategy.getNotUsedTransformedElement(mp1, op1, new Ingredient(base3));
			assertNotNull(ins3i);
			// assertEquals(i + 1,
			// Stats.currentStat.combinationByIngredientSize.keySet().size());
			System.out.println("After: " + Stats.currentStat.combinationByIngredientSize);
			int remainingSize = ingredientsAfterTransformation3Size - i + 1;// +1
																			// due
																			// i
																			// starts
																			// in
																			// 1
			assertTrue(" " + i, (long) Stats.currentStat.combinationByIngredientSize.get((long) (remainingSize)) >= 2);
			if (Stats.currentStat.combinationByIngredientSize.containsKey((long) 0)) {
				System.out.println("Putting zero for " + i + ", ing " + ins3i + ", total attepts with zero "
						+ Stats.currentStat.combinationByIngredientSize.get((long) 0));
			}
		}

		Ingredient ins3n = estrategy.getNotUsedTransformedElement(mp1, op1, new Ingredient(base3));
		assertNull(ins3n);

		System.out.println("Putting zero for " + base3 + " total attepts with zero "
				+ Stats.currentStat.combinationByIngredientSize.get((long) 0));

		assertTrue(estrategy.exhaustTemplates.get(estrategy.getKey(mp1, op1)).contains(base3));

		System.out.println("--------BASE 4--no trasformation--------");

		List<CtCodeElement> bases4 = estrategy.getNotExhaustedBaseElements(mp1, op1);
		assertFalse(bases4.contains(base3));

		///

		assertTrue(!estrategy.exhaustTemplates.get(estrategy.getKey(mp1, op1)).isEmpty());

		List l = (List) ingredientSpace.mkp.values().toArray()[0];
		CtCodeElement base4 = (CtCodeElement) l.get(0);
		assertEquals("clearResult()", base4.toString());

		long before = Stats.currentStat.combinationByIngredientSize.get((long) 1);
		ModificationPoint mp5 = cardumen.getVariants().get(0).getModificationPoints().get(5);
		List<Ingredient> ingredientsAfterTransformation4 = estrategy.getInstancesFromBase(mp5, op1,
				new Ingredient(base4));
		System.out.println("mp5 " + Stats.currentStat.combinationByIngredientSize);
		assertTrue(Stats.currentStat.combinationByIngredientSize.containsKey((long) 1));

		assertEquals(before + 1, Stats.currentStat.combinationByIngredientSize.get((long) 1), 0);

		assertTrue(ingredientsAfterTransformation4.size() > 0);

		System.out.println("--------BASE 5----------");

		SuspiciousModificationPoint mp7 = (SuspiciousModificationPoint) cardumen.getVariants().get(0)
				.getModificationPoints().get(7);
		assertTrue(mp7.getSuspicious().getClassName().contains("UnivariateRealSolverUtils"));
		assertEquals("solve(_UnivariateRealFunction_0, _double_1, _double_2)", base1.toString());

		assertFalse(Stats.currentStat.combinationByIngredientSize.containsKey((long) 0));

		List<Ingredient> ingredientsAfterTransformation5 = estrategy.getInstancesFromBase(mp7, op1,
				new Ingredient(base1));

		assertTrue(ingredientsAfterTransformation5.isEmpty());

		assertTrue(Stats.currentStat.combinationByIngredientSize.containsKey((long) 0));

		assertEquals(1, Stats.currentStat.combinationByIngredientSize.get((long) 0), 0);

	}

	@Test
	public void testCardumentM70Exhausitve() throws Exception {
		CommandSummary command = MathTests.getMath70Command();

		IngredientSpaceScope scope = IngredientSpaceScope.PACKAGE;

		command.command.put("-mode", ExecutionMode.custom.name());
		command.command.put("-flthreshold", "0.1");
		command.command.put("-maxtime", "60");
		command.command.put("-population", "1");
		command.command.put("-customengine", CardumenExhaustiveEngine4Stats.class.getCanonicalName());
		command.command.put("-scope", scope.toString().toLowerCase());
		command.command.put("-parameters",
				"limitbysuspicious:false:" + "disablelog:true:uniformreplacement:false:frequenttemplate:true");
		command.command.put("-loglevel", Level.DEBUG.toString());
		command.command.put("-maxVarCombination", "100000000");

		AstorMain main1 = new AstorMain();
		main1.execute(command.flat());
		Stats.createStat();
		CardumenExhaustiveEngine4Stats cardumen = (CardumenExhaustiveEngine4Stats) main1.getEngine();

		assertEquals(100605077, cardumen.totalIngredients);
		assertEquals(100605077, cardumen.totalIngredientsCutted);
		assertEquals(38222, cardumen.totalBases);
		assertEquals(0, cardumen.attemptsCutted);
	}

	@Test
	public void testCardumentM70ExhausitveMaxLimited() throws Exception {
		CommandSummary command = MathTests.getMath70Command();

		IngredientSpaceScope scope = IngredientSpaceScope.PACKAGE;
		// Configuration for paper experiment
		command.command.put("-mode", ExecutionMode.custom.name());
		command.command.put("-flthreshold", "0.1");
		command.command.put("-maxtime", "60");
		command.command.put("-population", "1");
		command.command.put("-customengine", CardumenExhaustiveEngine4Stats.class.getCanonicalName());
		command.command.put("-scope", scope.toString().toLowerCase());
		command.command.put("-parameters",
				"limitbysuspicious:false:" + "disablelog:true:uniformreplacement:false:frequenttemplate:true");
		command.command.put("-loglevel", Level.DEBUG.toString());
		command.command.put("-maxVarCombination", "1000");

		AstorMain main1 = new AstorMain();
		main1.execute(command.flat());
		Stats.createStat();
		CardumenExhaustiveEngine4Stats cardumen = (CardumenExhaustiveEngine4Stats) main1.getEngine();

		assertEquals(86299730, cardumen.totalIngredients);
		assertTrue(86299730 > cardumen.totalIngredientsCutted);
		assertEquals(26915, cardumen.totalIngredientsCutted);
		assertEquals(160, cardumen.totalBases);

	}

	@Test
	public void testCardumentM70ExhausitveMaxSuspiciousLimited() throws Exception {
		CommandSummary command = MathTests.getMath70Command();

		IngredientSpaceScope scope = IngredientSpaceScope.PACKAGE;

		command.command.put("-mode", ExecutionMode.custom.name());
		command.command.put("-flthreshold", "0.1");
		command.command.put("-maxtime", "60");
		command.command.put("-population", "1");
		command.command.put("-customengine", CardumenExhaustiveEngine4Stats.class.getCanonicalName());
		command.command.put("-scope", scope.toString().toLowerCase());
		command.command.put("-parameters", "skipfitnessinitialpopulation:true:limitbysuspicious:false:"
				+ "disablelog:false:uniformreplacement:false:frequenttemplate:false");
		command.command.put("-loglevel", Level.DEBUG.toString());
		command.command.put("-maxVarCombination", "100");
		command.command.put("-maxsuspcandidates", "1000");

		AstorMain main1 = new AstorMain();
		main1.execute(command.flat());
		Stats.createStat();
		CardumenExhaustiveEngine4Stats cardumen = (CardumenExhaustiveEngine4Stats) main1.getEngine();

		assertEquals(12, cardumen.totalmp);

		command.command.put("-maxsuspcandidates", "3");
		main1.execute(command.flat());
		Stats.createStat();
		cardumen = (CardumenExhaustiveEngine4Stats) main1.getEngine();
		assertEquals(3, cardumen.totalmp);

		// assertEquals(100605077,cardumen.totalIngredients);
		// assertTrue(100605077 > cardumen.totalIngredientsCutted);
		// assertEquals(38222,cardumen.totalBases);

	}

	@Test
	public void testCardumentM70MaxModPoints() throws Exception {
		CommandSummary command = MathTests.getMath70Command();

		IngredientSpaceScope scope = IngredientSpaceScope.PACKAGE;

		int maxModPoints = 7;// Let's say 7, the number of MP over this
								// configuration is 12.

		command.command.put("-mode", ExecutionMode.custom.name());
		command.command.put("-flthreshold", "0.1");
		command.command.put("-maxgen", "0");
		command.command.put("-population", "1");
		command.command.put("-customengine", CardumenExhaustiveEngine4Stats.class.getCanonicalName());
		command.command.put("-scope", scope.toString().toLowerCase());
		command.command.put("-parameters",
				"maxmodificationpoints:" + maxModPoints + ":skipfitnessinitialpopulation:true:limitbysuspicious:false");
		command.command.put("-loglevel", Level.DEBUG.toString());

		AstorMain main1 = new AstorMain();
		main1.execute(command.flat());
		Stats.createStat();

		assertEquals(maxModPoints, main1.getEngine().getVariants().get(0).getModificationPoints().size());
	}

	@Test
	public void testCardumentM70ExhausitveReplacement() throws Exception {
		CommandSummary command = MathTests.getMath70Command();

		IngredientSpaceScope scope = IngredientSpaceScope.PACKAGE;
		boolean uniformreplacement = false;
		command.command.put("-mode", ExecutionMode.custom.name());
		command.command.put("-flthreshold", "0.1");
		command.command.put("-maxtime", "60");
		command.command.put("-population", "1");
		command.command.put("-customengine", CardumenExhaustiveEngine4Stats.class.getCanonicalName());
		command.command.put("-scope", scope.toString().toLowerCase());
		command.command.put("-parameters", "skipfitnessinitialpopulation:true:limitbysuspicious:false:"
				+ "disablelog:false:uniformreplacement:" + Boolean.toString(uniformreplacement));
		command.command.put("-loglevel", Level.DEBUG.toString());

		AstorMain main1 = new AstorMain();
		main1.execute(command.flat());
		Stats.createStat();
		assertFalse(ConfigurationProperties.getPropertyBool("uniformreplacement"));

		CardumenExhaustiveEngine4Stats cardumen = (CardumenExhaustiveEngine4Stats) main1.getEngine();

		assertEquals(12, cardumen.totalmp);
		long tingNotUnif = cardumen.totalIngredientsCutted;
		long tingNotUnifall = cardumen.totalIngredients;

		// changing property

		uniformreplacement = true;

		command.command.put("-parameters", "skipfitnessinitialpopulation:true:limitbysuspicious:false:"
				+ "disablelog:false:uniformreplacement:" + Boolean.toString(uniformreplacement));

		cardumen = null;
		main1.execute(command.flat());
		Stats.createStat();
		cardumen = (CardumenExhaustiveEngine4Stats) main1.getEngine();
		assertTrue(ConfigurationProperties.getPropertyBool("uniformreplacement"));
		long tingUnif = cardumen.totalIngredientsCutted;
		long tingUnifall = cardumen.totalIngredients;
		System.out.println(tingNotUnif + " > " + tingUnif);
		assertTrue(tingNotUnif + " > " + tingUnif, tingNotUnif > tingUnif);
		System.out.println(tingNotUnifall + " > " + tingUnifall);
		assertTrue(tingNotUnifall + " > " + tingUnifall, tingNotUnifall > tingUnifall);
	}

	@Test
	@Ignore
	public void testCardumentM42() throws Exception {
		CommandSummary command = MathTests.getMath42Command();

		IngredientSpaceScope scope = IngredientSpaceScope.PACKAGE;
		boolean uniformreplacement = false;
		command.command.put("-mode", ExecutionMode.custom.name());
		command.command.put("-flthreshold", "0.1");
		command.command.put("-maxtime", "60");
		command.command.put("-population", "1");
		command.command.put("-customengine", CardumenExhaustiveEngine4Stats.class.getCanonicalName());
		command.command.put("-scope", scope.toString().toLowerCase());
		command.command.put("-parameters", "skipfitnessinitialpopulation:true:limitbysuspicious:false:"
				+ "disablelog:false:uniformreplacement:" + Boolean.toString(uniformreplacement));
		command.command.put("-loglevel", Level.DEBUG.toString());

		AstorMain main1 = new AstorMain();
		main1.execute(command.flat());
		Stats.createStat();

	}

}
