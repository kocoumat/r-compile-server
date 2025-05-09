package org.prlprg.bc2ir;

import static org.prlprg.bc2ir.ClosureCompiler.compileBaselineClosure;
import static org.prlprg.bc2ir.ClosureCompiler.compilePromise;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.prlprg.bc.Bc;
import org.prlprg.bc.BcInstr;
import org.prlprg.bc.BcInstr.Add;
import org.prlprg.bc.BcInstr.And;
import org.prlprg.bc.BcInstr.And1st;
import org.prlprg.bc.BcInstr.And2nd;
import org.prlprg.bc.BcInstr.BaseGuard;
import org.prlprg.bc.BcInstr.BrIfNot;
import org.prlprg.bc.BcInstr.CallBuiltin;
import org.prlprg.bc.BcInstr.CallSpecial;
import org.prlprg.bc.BcInstr.CheckFun;
import org.prlprg.bc.BcInstr.Colon;
import org.prlprg.bc.BcInstr.DdVal;
import org.prlprg.bc.BcInstr.DdValMissOk;
import org.prlprg.bc.BcInstr.DecLnk;
import org.prlprg.bc.BcInstr.DecLnkStk;
import org.prlprg.bc.BcInstr.DeclnkN;
import org.prlprg.bc.BcInstr.DfltC;
import org.prlprg.bc.BcInstr.DfltSubassign;
import org.prlprg.bc.BcInstr.DfltSubassign2;
import org.prlprg.bc.BcInstr.DfltSubset;
import org.prlprg.bc.BcInstr.DfltSubset2;
import org.prlprg.bc.BcInstr.Div;
import org.prlprg.bc.BcInstr.DoDots;
import org.prlprg.bc.BcInstr.DoLoopBreak;
import org.prlprg.bc.BcInstr.DoLoopNext;
import org.prlprg.bc.BcInstr.DoMissing;
import org.prlprg.bc.BcInstr.Dollar;
import org.prlprg.bc.BcInstr.DollarGets;
import org.prlprg.bc.BcInstr.DotCall;
import org.prlprg.bc.BcInstr.DotsErr;
import org.prlprg.bc.BcInstr.Dup;
import org.prlprg.bc.BcInstr.Dup2nd;
import org.prlprg.bc.BcInstr.EndAssign;
import org.prlprg.bc.BcInstr.EndAssign2;
import org.prlprg.bc.BcInstr.EndFor;
import org.prlprg.bc.BcInstr.EndLoopCntxt;
import org.prlprg.bc.BcInstr.Eq;
import org.prlprg.bc.BcInstr.Exp;
import org.prlprg.bc.BcInstr.Expt;
import org.prlprg.bc.BcInstr.Ge;
import org.prlprg.bc.BcInstr.GetBuiltin;
import org.prlprg.bc.BcInstr.GetFun;
import org.prlprg.bc.BcInstr.GetGlobFun;
import org.prlprg.bc.BcInstr.GetIntlBuiltin;
import org.prlprg.bc.BcInstr.GetSymFun;
import org.prlprg.bc.BcInstr.GetVar;
import org.prlprg.bc.BcInstr.GetVarMissOk;
import org.prlprg.bc.BcInstr.GetterCall;
import org.prlprg.bc.BcInstr.Goto;
import org.prlprg.bc.BcInstr.Gt;
import org.prlprg.bc.BcInstr.IncLnk;
import org.prlprg.bc.BcInstr.IncLnkStk;
import org.prlprg.bc.BcInstr.Invisible;
import org.prlprg.bc.BcInstr.IsCharacter;
import org.prlprg.bc.BcInstr.IsComplex;
import org.prlprg.bc.BcInstr.IsDouble;
import org.prlprg.bc.BcInstr.IsInteger;
import org.prlprg.bc.BcInstr.IsLogical;
import org.prlprg.bc.BcInstr.IsNull;
import org.prlprg.bc.BcInstr.IsNumeric;
import org.prlprg.bc.BcInstr.IsObject;
import org.prlprg.bc.BcInstr.IsSymbol;
import org.prlprg.bc.BcInstr.LdConst;
import org.prlprg.bc.BcInstr.LdFalse;
import org.prlprg.bc.BcInstr.LdNull;
import org.prlprg.bc.BcInstr.LdTrue;
import org.prlprg.bc.BcInstr.Le;
import org.prlprg.bc.BcInstr.Log;
import org.prlprg.bc.BcInstr.LogBase;
import org.prlprg.bc.BcInstr.Lt;
import org.prlprg.bc.BcInstr.MakeClosure;
import org.prlprg.bc.BcInstr.MakeProm;
import org.prlprg.bc.BcInstr.MatSubassign;
import org.prlprg.bc.BcInstr.MatSubassign2;
import org.prlprg.bc.BcInstr.MatSubset;
import org.prlprg.bc.BcInstr.MatSubset2;
import org.prlprg.bc.BcInstr.Math1;
import org.prlprg.bc.BcInstr.Mul;
import org.prlprg.bc.BcInstr.Ne;
import org.prlprg.bc.BcInstr.Not;
import org.prlprg.bc.BcInstr.Or;
import org.prlprg.bc.BcInstr.Or1st;
import org.prlprg.bc.BcInstr.Or2nd;
import org.prlprg.bc.BcInstr.Pop;
import org.prlprg.bc.BcInstr.PrintValue;
import org.prlprg.bc.BcInstr.PushArg;
import org.prlprg.bc.BcInstr.PushConstArg;
import org.prlprg.bc.BcInstr.PushFalseArg;
import org.prlprg.bc.BcInstr.PushNullArg;
import org.prlprg.bc.BcInstr.PushTrueArg;
import org.prlprg.bc.BcInstr.Return;
import org.prlprg.bc.BcInstr.ReturnJmp;
import org.prlprg.bc.BcInstr.SeqAlong;
import org.prlprg.bc.BcInstr.SeqLen;
import org.prlprg.bc.BcInstr.SetLoopVal;
import org.prlprg.bc.BcInstr.SetTag;
import org.prlprg.bc.BcInstr.SetVar;
import org.prlprg.bc.BcInstr.SetVar2;
import org.prlprg.bc.BcInstr.SetterCall;
import org.prlprg.bc.BcInstr.SpecialSwap;
import org.prlprg.bc.BcInstr.Sqrt;
import org.prlprg.bc.BcInstr.StartAssign;
import org.prlprg.bc.BcInstr.StartAssign2;
import org.prlprg.bc.BcInstr.StartC;
import org.prlprg.bc.BcInstr.StartFor;
import org.prlprg.bc.BcInstr.StartLoopCntxt;
import org.prlprg.bc.BcInstr.StartSubassign;
import org.prlprg.bc.BcInstr.StartSubassign2;
import org.prlprg.bc.BcInstr.StartSubassign2N;
import org.prlprg.bc.BcInstr.StartSubassignN;
import org.prlprg.bc.BcInstr.StartSubset;
import org.prlprg.bc.BcInstr.StartSubset2;
import org.prlprg.bc.BcInstr.StartSubset2N;
import org.prlprg.bc.BcInstr.StartSubsetN;
import org.prlprg.bc.BcInstr.StepFor;
import org.prlprg.bc.BcInstr.Sub;
import org.prlprg.bc.BcInstr.Subassign2N;
import org.prlprg.bc.BcInstr.SubassignN;
import org.prlprg.bc.BcInstr.Subset2N;
import org.prlprg.bc.BcInstr.SubsetN;
import org.prlprg.bc.BcInstr.Switch;
import org.prlprg.bc.BcInstr.UMinus;
import org.prlprg.bc.BcInstr.UPlus;
import org.prlprg.bc.BcInstr.VecSubassign;
import org.prlprg.bc.BcInstr.VecSubassign2;
import org.prlprg.bc.BcInstr.VecSubset;
import org.prlprg.bc.BcInstr.VecSubset2;
import org.prlprg.bc.BcInstr.Visible;
import org.prlprg.bc.BcLabel;
import org.prlprg.bc.ConstPool;
import org.prlprg.bc.LabelName;
import org.prlprg.ir.cfg.BB;
import org.prlprg.ir.cfg.CFG;
import org.prlprg.ir.cfg.Constant;
import org.prlprg.ir.cfg.FrameState;
import org.prlprg.ir.cfg.IsEnv;
import org.prlprg.ir.cfg.JumpData;
import org.prlprg.ir.cfg.Node;
import org.prlprg.ir.cfg.Phi;
import org.prlprg.ir.cfg.RValue;
import org.prlprg.ir.cfg.StaticEnv;
import org.prlprg.ir.cfg.Stmt;
import org.prlprg.ir.cfg.StmtData;
import org.prlprg.ir.cfg.StmtData.FrameState_;
import org.prlprg.ir.cfg.StmtData.GnuRIs;
import org.prlprg.ir.cfg.StmtData.NamelessCall;
import org.prlprg.ir.cfg.StmtData.RValue_;
import org.prlprg.ir.cfg.builder.CFGCursor;
import org.prlprg.ir.closure.Closure;
import org.prlprg.ir.closure.Promise;
import org.prlprg.primitive.BuiltinId;
import org.prlprg.primitive.IsTypeCheck;
import org.prlprg.rshruntime.BcPosition;
import org.prlprg.sexp.Attributes;
import org.prlprg.sexp.LangSXP;
import org.prlprg.sexp.ListSXP;
import org.prlprg.sexp.RegSymSXP;
import org.prlprg.sexp.SEXP;
import org.prlprg.sexp.SEXPs;
import org.prlprg.util.Lists;
import org.prlprg.util.Reflection;
import org.prlprg.util.Strings;
import org.prlprg.util.UnreachableError;

/**
 * Stores data to compile {@linkplain Bc bytecode} into an {@linkplain org.prlprg.ir IR} {@linkplain
 * CFG control-flow-graph}.
 */
public class CFGCompiler {
  /**
   * Compile {@linkplain Bc bytecode} into an {@linkplain org.prlprg.ir IR} {@linkplain CFG
   * control-flow-graph}.
   *
   * <p>This assumes that the bytecode is for a closure.
   *
   * @throws UnsupportedOperationException If any inner closures or promise bodies are ASTs (not
   *     produced by the Java compiler unless the ASTs can't be bytecode, but maybe possible).
   */
  public static CFG compileCFG(Bc bc) {
    var cfg = new CFG();
    var module = new Module();
    new CFGCompiler(bc, cfg, false, module).doCompile();
    return cfg;
  }

  /**
   * Compile {@linkplain Bc bytecode} into the given {@linkplain org.prlprg.ir IR} {@linkplain CFG
   * control-flow-graph}.
   */
  static void compileCFG(Bc bc, CFG cfg, boolean isPromise, Module module) {
    new CFGCompiler(bc, cfg, isPromise, module).doCompile();
  }

  // region compiler data types
  // - ADTs to organize the compiler data.

  /**
   * Stores information about a loop being compiled which is shared between multiple instructions.
   */
  private record Loop(LoopType type, @Nullable BB forBody, BB next, BB end) {}

  /** Whether a loop is a GNU-R "while" or "for" loop. */
  private enum LoopType {
    WHILE_OR_REPEAT,
    FOR
  }

  /**
   * A pending complex assignment (between {@link StartAssign} and {@link EndAssign}, or {@link
   * StartAssign2} and {@link EndAssign2}).
   */
  private record ComplexAssign(boolean isSuper, RegSymSXP name, RValue originalRhs) {}

  /**
   * A call currently being built.
   *
   * <p>The function is set on construction, but the argument stack is built after.
   */
  private static class Call {
    private final Fun fun;
    private final List<Arg> args = new ArrayList<>();

    public Call(Fun fun) {
      this.fun = fun;
    }

    sealed interface Fun {
      record Value(RValue fun) implements Fun {}

      record Builtin(BuiltinId id) implements Fun {}
    }

    /** Stores information about a call argument: a {@link RValue} node and optional name. */
    private record Arg(RValue value, @Nullable String name) {
      static Arg missing() {
        return new Arg(new Constant(SEXPs.MISSING_ARG), null);
      }

      Arg(RValue value) {
        this(value, null);
      }
    }

    @Override
    public String toString() {
      return "Call{" + "fun=" + fun + ", args=" + args + '}';
    }
  }

  /**
   * Stores information about a dispatch function being compiled which is shared between multiple
   * instructions.
   */
  private record Dispatch(Dispatch.Type type, LangSXP ast, BB after) {

    /** The dispatch function (e.g. "[", "[<-", "c"). */
    private enum Type {
      C(BuiltinId.C, false, false),
      SUBSET(BuiltinId.SUBSET, false, false),
      SUBASSIGN(BuiltinId.SUBASSIGN, true, false),
      SUBSET2(BuiltinId.SUBSET2, false, false),
      SUBASSIGN2(BuiltinId.SUBASSIGN2, true, false),
      SUBSETN(BuiltinId.SUBSETN, false, true),
      SUBASSIGNN(BuiltinId.SUBASSIGNN, true, true),
      SUBSET2N(BuiltinId.SUBSET2N, false, true),
      SUBASSIGN2N(BuiltinId.SUBASSIGN2N, true, true);

      private final BuiltinId builtin;
      private final boolean hasRhs;
      private final boolean isNForm;

      Type(BuiltinId builtin, boolean hasRhs, boolean isNForm) {
        this.builtin = builtin;
        this.hasRhs = hasRhs;
        this.isNForm = isNForm;
      }
    }
  }

  // endregion compiler data types

  // region compiler data
  // - Some of it is constant through the compilation, some changes as instructions are compiled.
  private final Module module;
  private final Bc bc;
  private final CFG cfg;
  private final boolean isPromise;
  private final RValue env;
  private final Map<Integer, BB> bbByLabel = new HashMap<>();
  private final Set<BB> bbsWithPhis = new HashSet<>();
  private int bcPos = 0;
  private boolean isInUnreachableBytecode = false;
  private final CFGCursor cursor;
  private final List<Node> stack = new ArrayList<>();
  private final List<Loop> loopStack = new ArrayList<>();
  private final List<ComplexAssign> complexAssignStack = new ArrayList<>();
  private final List<Call> callStack = new ArrayList<>();
  private final List<Dispatch> dispatchStack = new ArrayList<>();

  // endregion compiler data

  // region main compile functions: compile everything
  // ???: Is `LdFunctionEnv` correct for functions? It seems to only be for promises.

  /**
   * Create a compiler.
   *
   * <p>After this, it initialized the variables necessary for compiling but has only put the "load
   * function environment" instruction.
   */
  private CFGCompiler(Bc bc, CFG cfg, boolean isPromise, Module module) {
    if (!cfg.isEmpty()) {
      throw new IllegalArgumentException(
          "CFG must be empty (idk when you'd call CFGCompiler with a non-empty CFG)");
    }

    this.module = module;
    this.bc = bc;
    this.cfg = cfg;
    this.isPromise = isPromise;

    cursor = new CFGCursor(cfg);

    // Insert `LdFunctionEnv`, which goes before all bytecode instructions.
    env = cursor.insert("enclos", new StmtData.LdFunctionEnv());
    var entry_ = cfg.addBB("entry_");
    cursor.insert(new JumpData.Goto(entry_));
    cursor.moveToStart(entry_);
    bbByLabel.put(0, entry_);
  }

  /** Actually compile {@code bc} into {@code cfg}. */
  void doCompile() {
    try {
      var code = bc.code();

      // Put BBs at jump destinations.
      // We will add more BBs when compiling some instructions, but IR in those BBs will be specific
      // to the instruction, whereas labels in instructions may jump to within the IR of previously-
      // compiled instructions. To insert a BB later that jumps to previously-compiled IR: it's
      // possible, but adds complexity, because we have to remember which bytecode positions
      // correspond to which IR positions at these positions, and move already-compiled IR into the
      // new BB. It's easier to insert these BBs first, then we don't have to move IR and we only
      // need
      // to track the current bytecode and IR position.
      for (var i = 0; i < code.size(); i++) {
        addBcLabelBBs(code.get(i));
      }

      // Add instructions to BBs, including jumps which connect them.
      // Also, move to the BB at each label, and skip unreachable bytecode.
      for (bcPos = 0; bcPos < code.size(); bcPos++) {
        if (bbByLabel.containsKey(bcPos) && bbByLabel.get(bcPos) != cursor.bb()) {
          // Move to the BB at this label.
          var nextBb = bbByLabel.get(bcPos);
          if (cursor.bb().jump() == null) {
            // We need to insert a jump because in the bytecode it just falls through, but there's
            // no such thing as fallthrough in IR.
            cursor.insert(new JumpData.Goto(nextBb));
          }
          if (nextBb.predecessors().isEmpty() && !bbsWithPhis.contains(nextBb)) {
            // This happens after a compiled `repeat`, so the following bytecode is unreachable and
            // we can even remove `nextBb` (since it's entirely unreachable). Maybe it's not correct
            // in other cases.
            isInUnreachableBytecode = true;
            cfg.remove(nextBb);
          } else {
            // If we were skipping over unreachable bytecode, stop doing that.
            isInUnreachableBytecode = false;
            // Then move the cursor to the next BB, and change the stack for it.
            moveTo(nextBb);
          }
        }
        if (!isInUnreachableBytecode) {
          // Add instruction, it's not unreachable.
          addBcInstrIrInstrs(code.get(bcPos));
        }
      }
    } catch (Throwable e) {
      throw e instanceof CFGCompilerException e1
          ? e1
          : new CFGCompilerException("uncaught exception: " + e, bc, bcPos, cursor, e);
    }

    // We do optimizations later, but cleanup now so we can verify the CFG.
    cfg.cleanup();
  }

  // endregion main compile functions: compile everything

  // region map BBs to labels (+ get)

  /**
   * Add and map a basic block to every label referenced by an instruction.
   *
   * <p>A jump to a label in bytecode compiles to a jump to the mapped basic block in IR. Some
   * bytecode instructions compile into more jumps and basic blocks, but those basic blocks only
   * contain, and jumps only go to, IR compiled from that instruction and from future instructions.
   * The only scenario we need to insert a jump into IR from previously-compiled instructions, is
   * when we insert a jump to a label; we exploit this and insert basic blocks for all of the labels
   * before compiling the instructions, so that we know ahead-of-time which block every
   * instruction's compiled IR belongs to.
   *
   * <p>This properly handles the case where multiple instructions have the same label, by not
   * inserting a new block when it encounters again a label that it already inserted a block for
   * (although maybe this can't happen in bytecode so we don't need to handle it...).
   */
  private void addBcLabelBBs(BcInstr instr) {
    if (!(instr instanceof Record r)) {
      throw new AssertionError("bytecode instructions should all be java records");
    }

    if (instr instanceof Switch(var _, var _, var chrLabelsIdx, var numLabelsIdx)) {
      // `switch` is special, because the labels are encoded in the constant pool.
      var chrLabels = chrLabelsIdx == null ? null : get(chrLabelsIdx);
      var numLabels = numLabelsIdx == null ? null : get(numLabelsIdx);
      if (chrLabels != null) {
        for (var i = 0; i < chrLabels.size(); i++) {
          var labelPos = chrLabels.get(i);
          ensureBbAt(new BcLabel(labelPos), "switchChr" + i);
        }
      }
      if (numLabels != null) {
        for (var i = 0; i < numLabels.size(); i++) {
          var labelPos = numLabels.get(i);
          ensureBbAt(new BcLabel(labelPos), "switchNum" + i);
        }
      }
      return;
    }

    for (var component : instr.getClass().getRecordComponents()) {
      var clazz = component.getType();
      var labelName = component.getAnnotation(LabelName.class);
      var value = Reflection.getComponent(r, component);
      assert clazz == BcLabel.class || labelName == null
          : "only BcLabel fields should be annotated with @LabelName";
      assert clazz != BcLabel.class || labelName != null
          : "BcLabel fields should be annotated with @LabelName";

      if (clazz == BcLabel.class) {
        ensureBbAt((BcLabel) value, labelName.value());
      }
    }
  }

  /**
   * Adds a basic block and maps it to the given label, if a basic block for the given label doesn't
   * already exist (otherwise no-op).
   */
  private void ensureBbAt(BcLabel label, String name) {
    int pos = label.target();
    if (!bbByLabel.containsKey(pos)) {
      bbByLabel.put(pos, cfg.addBB(name));
    }
  }

  /**
   * Returns the basic block corresponding to the given label
   *
   * <p>Specifically, this returns the block inserted by {@link #ensureBbAt(BcLabel, String)}, which
   * should've been compiled before we started "actually" compiling the bytecode instructions, and
   * this is called while actually compiling the instructions.
   */
  private BB bbAt(BcLabel label) {
    int pos = label.target();
    assert bbByLabel.containsKey(pos) : "no BB at position " + pos;
    return bbByLabel.get(pos);
  }

  /**
   * Whether there is a label pointing immediately after the current bytecode instruction.
   *
   * <p>Specifically, returns whether there is a BB in {@code bbByLabel} mapped to the position
   * immediately after.
   */
  private boolean hasBbAfterCurrent() {
    return bbByLabel.containsKey(bcPos + 1);
  }

  /**
   * Gets the basic block immediately after the current bytecode instruction.
   *
   * <p>Specifically, returns the BB in {@code bbByLabel} mapped to the position immediately after,
   * creating it if this mapping doesn't exist.
   */
  private BB bbAfterCurrent() {
    return bbByLabel.computeIfAbsent(bcPos + 1, _ -> cfg.addBB());
  }

  // endregion map BBs to labels (+ get)

  // region main compile functions: compile individual things
  private void addBcInstrIrInstrs(BcInstr instr) {
    switch (instr) {
      case Return() -> {
        var retVal = pop(RValue.class);
        assertStacksForReturn();
        cursor.insert(new JumpData.Return(retVal));
        setInUnreachableBytecode();
      }
      case Goto(var label) -> {
        var bb = bbAt(label);
        cursor.insert(new JumpData.Goto(bb));
        addPhiInputsForStack(bb);
        setInUnreachableBytecode();
      }
      case BrIfNot(var ast, var label) -> {
        var bb = bbAt(label);
        cursor.insert(new JumpData.Branch(get(ast), pop(RValue.class), bbAfterCurrent(), bb));
        addPhiInputsForStack(bb);
      }
      case Pop() -> pop(Node.class);
      case Dup() -> {
        var value = pop(Node.class);
        push(value);
        push(value);
      }
      case PrintValue() ->
          pushInsert(
              new StmtData.CallBuiltin(
                  Optional.empty(),
                  BuiltinId.PRINT_VALUE,
                  ImmutableList.of(pop(RValue.class)),
                  env));
      case StartLoopCntxt(var isForLoop, var end) -> {
        // REACH: Complicated loop contexts.
        // Currently we only handle simple cases like PIR, where `next` and `break` aren't in
        // promises. To do this (handle the non-promise cases and fail on the promise ones), we
        // maintain a stack of loops (markers, `cond` and `end` blocks) within each `CFGCompiler`.
        // In the simple non-promise cases, we encounter `break` or `next` while the loop stack has
        // an element, which is the inner-most loop that we want to break or continue. In a promise,
        // if there is no inner loop, the loop stack is empty, so we know to fail and can report the
        // failure accurately.

        // We already handle `for` loops in `StartFor` (due to the very specific bytecode pattern of
        // for loops), but need to handle `while` and `repeat` since they don't have specific
        // bytecode instructions.
        if (isForLoop) {
          // Specifically, we step over this instruction if we encounter `StartFor`, so it's never
          // compiled directly unless the bytecode is malformed.
          throw fail("StartLoopCntxt: expected to be preceded by StartFor");
        }

        // This takes advantage of the invariant that the loop body is right after
        // `StartLoopCntxt`, so a label should exist here.
        var bodyBb = bbAfterCurrent();
        var endBb = bbAt(end);

        pushWhileOrRepeatLoop(bodyBb, endBb);
      }
      case EndLoopCntxt(var isForLoop) -> {
        // We already handle `for` loops in `EndFor` (similar to `StartLoopCntxt`).
        // We don't auto-skip over them though because we don't need to.
        if (!isForLoop) {
          compileEndLoop(LoopType.WHILE_OR_REPEAT);
        }
      }
      case StartFor(var ast, var elemName, var step) -> {
        // This takes advantage of invariants expected between `StartFor/StepFor/EndFor` and just
        // compiles the entire for loop logic (everything other than the loop forBody). We then do a
        // weak sanity check that the bytecode upholds these invariants by including `StepFor` in
        // the step branch and `EndFor` in the end branch, although it may break the invariants
        // other ways and simply not be noticed.
        var loopCntxt = bc.code().get(bcPos + 1) instanceof StartLoopCntxt l ? l : null;

        var initBb = cursor.bb();
        BB forBodyBb;
        BB stepBb;
        BB endBb;
        if (loopCntxt == null) {
          forBodyBb = bbAfterCurrent();
          stepBb = bbAt(step);
          endBb = cfg.addBB("endFor");
        } else {
          var stepGoto = bc.code().get(bcPos + 2) instanceof Goto g ? g : null;
          if (stepGoto == null) {
            throw fail(
                "StartFor followed by StartLoopCntxt, expected the StartLoopCntxt to be followed by Goto");
          }
          if (!bbByLabel.containsKey(bcPos + 3)) {
            throw fail(
                "StartFor followed by StartLoopCntxt followed by Goto, expected a label to be immediately after the Goto");
          }
          forBodyBb = bbByLabel.get(bcPos + 3);
          stepBb = bbAt(stepGoto.dest());
          endBb = bbAt(loopCntxt.end());
        }

        // For loop init
        var seq = cursor.insert(new StmtData.ToForSeq(pop(RValue.class)));
        var length = cursor.insert(new StmtData.Length(seq));
        var init = Constant.integer(0);
        cursor.insert(new JumpData.Goto(stepBb));

        // For loop step
        moveTo(stepBb);
        var index = stepBb.addPhi(RValue.class);
        index.rename("index");
        index.setInput(initBb, init);
        // Increment the index
        var index1 = cursor.insert(new StmtData.Inc(index.cast()));
        index1.rename("index");
        push(index1);
        // Compare the index to the length
        var cond = cursor.insert(new StmtData.Lt(Optional.of(get(ast)), length, index1, env));
        // Jump to `end` if it's greater (remember, GNU-R indexing is one-based)
        cursor.insert(new JumpData.Branch(get(ast), cond, endBb, forBodyBb));

        // For loop body
        moveTo(forBodyBb);
        // Extract element at index
        var elem = cursor.insert(new StmtData.Extract2_1D(Optional.of(get(ast)), seq, index1, env));
        // Store in the element variable
        cursor.insert(new StmtData.StVar(get(elemName), elem, env));
        // Now we compile the rest of the body...
        // When we encounter `StepFor`, we know that the body is over. Note that we may no longer be
        // in `forBodyBb`: that is just the first BB in the for body, there may be more.
        pushForLoop(forBodyBb, stepBb, endBb);

        if (loopCntxt != null) {
          // We already handled the next 2 instructions.
          // The `StartLoopCntxt` could be made into a no-op, but the `Goto` would break this setup
          // because we've already added the goto to `stepBb` and moved to `forBodyBb`.
          bcPos += 2;
        }
      }
      case DoLoopNext() -> {
        // The bytecode compilers don't actually generate these instructions, are they deprecated?
        // Regardless, we can still support them.
        if (isPromise && !inLocalLoop()) {
          throw failUnsupported("`next` or `break` in promise (complex loop context)");
        }
        var stepBb = topLoop().next;
        cursor.insert(new JumpData.Goto(stepBb));
        addPhiInputsForStack(stepBb);

        setInUnreachableBytecode();
      }
      case DoLoopBreak() -> {
        // The bytecode compilers don't actually generate these instructions, are they deprecated?
        // Regardless, we can still support them.
        if (isPromise && !inLocalLoop()) {
          throw failUnsupported("`next` or `break` in promise (complex loop context)");
        }
        var endBb = topLoop().end;
        cursor.insert(new JumpData.Goto(endBb));
        addPhiInputsForStack(endBb);

        setInUnreachableBytecode();
      }
      case StepFor(var body) -> {
        // This is the instruction immediately after the end of the for loop body (excluding `next`
        // and `break`).

        // First do a sanity check
        var loop = topLoop();
        if (loop.type != LoopType.FOR) {
          throw fail("StepFor: expected a for loop");
        }
        var forBodyBb = bbAt(body);
        var stepBb = loop.next;
        var endBb = loop.end;
        if (forBodyBb != loop.forBody) {
          throw fail(
              "StepFor: expected forBody BB "
                  + Objects.requireNonNull(loop.forBody).id()
                  + " but got "
                  + forBodyBb.id());
        }
        if (cursor.bb() != stepBb || cursor.stmtIdx() != 0) {
          throw fail("StepFor: expected to be at start of step BB " + stepBb.id());
        }

        // We must explicitly move to `endBb` because there may not be a label corresponding to it,
        // and we don't want to compile anything else in `stepBb`.
        moveTo(endBb);
      }
      case EndFor() -> compileEndLoop(LoopType.FOR);
      case SetLoopVal() -> {
        // This one is weird. It doesn't get inserted by the Java compiler anywhere.
        // However it has a simple (unhelpful) implementation, so even if unused, we'll support it.
        pop(RValue.class);
        pop(RValue.class);
        push(new Constant(SEXPs.NULL));
      }
      case Invisible() -> insert(new StmtData.Invisible());
      case LdConst(var constant) -> push(new Constant(get(constant)));
      case LdNull() -> push(new Constant(SEXPs.NULL));
      case LdTrue() -> push(new Constant(SEXPs.TRUE));
      case LdFalse() -> push(new Constant(SEXPs.FALSE));
      case GetVar(var name) -> {
        pushInsert(name, new StmtData.LdVar(get(name), false, env));
        pushInsert(new StmtData.Force(pop(RValue.class), compileFrameState(), env));
      }
      case DdVal(var name) -> {
        var ddIndex = get(name).ddNum();
        pushInsert(name, new StmtData.LdDdVal(ddIndex, false, env));
        pushInsert(new StmtData.Force(pop(RValue.class), compileFrameState(), env));
      }
      case SetVar(var name) -> insert(new StmtData.StVar(get(name), top(RValue.class), env));
      case GetFun(var name) -> pushCallFunInsert(name, new StmtData.LdFun(get(name), env));
      case GetGlobFun(var name) ->
          pushCallFunInsert(name, new StmtData.LdFun(get(name), StaticEnv.GLOBAL));
        // ???: GNU-R calls `SYMVALUE` and `INTERNAL` to implement these, but we don't store that in
        //  our `RegSymSxp` data-structure. So the next three implementations may be incorrect.
      case GetSymFun(var name) -> pushCall(BuiltinId.referencedBy(get(name)));
      case GetBuiltin(var name) -> pushCall(BuiltinId.referencedBy(get(name)));
      case GetIntlBuiltin(var name) -> pushCall(BuiltinId.referencedBy(get(name)));
      case CheckFun() -> pushCallFunInsert(new StmtData.ChkFun(pop(RValue.class)));
      case MakeProm(var code) -> {
        // REACH: Could cache compiled promise CFGs (env will always be different) in the module...
        // TODO: infer name
        Promise promise;
        try {
          promise = compilePromise("", get(code), env, module);
        } catch (ClosureCompilerUnsupportedException e) {
          throw failUnsupported("Promise with unsupported body", e);
        }
        pushCallArgInsert(new StmtData.MkProm(promise));
      }
      case DoMissing() -> pushMissingCallArg();
      case SetTag(var tag) -> {
        if (tag != null) {
          setNameOfLastCallArg(
              get(tag)
                  .reifyString()
                  .orElseThrow(() -> fail("SetTag: tag must be a regular symbol or string")));
        }
      }
      case DoDots() -> pushCallArgInsert("...", new StmtData.LdDots(env));
      case PushArg() -> pushCallArg(pop(RValue.class));
      case PushConstArg(var constant) -> pushCallArg(new Constant(get(constant)));
      case PushNullArg() -> pushCallArg(new Constant(SEXPs.NULL));
      case PushTrueArg() -> pushCallArg(new Constant(SEXPs.TRUE));
      case PushFalseArg() -> pushCallArg(new Constant(SEXPs.FALSE));
      case BcInstr.Call(var ast) -> compileCall(ast);
      case CallBuiltin(var ast) -> compileCall(ast);
      case CallSpecial(var astId) -> {
        var ast = get(astId);
        if (!(ast.fun() instanceof RegSymSXP builtinSymbol)) {
          throw fail("CallSpecial: expected a symbol (builtin id) function");
        }
        var builtin = BuiltinId.referencedBy(builtinSymbol);
        // Note that some of these are constant symbols and language objects, they should be treated
        // like `eval`. We also assume builtins never use names.
        // GNU-R just passes `CDR(call)` to the builtin.
        var args =
            ast.args().stream()
                .map(arg -> (RValue) new Constant(arg.value()))
                .collect(ImmutableList.toImmutableList());
        pushInsert(new StmtData.CallBuiltin(ast, builtin, args, env));
      }
      case MakeClosure(var arg) -> {
        var fb = get(arg);
        var forms = (ListSXP) fb.get(0);
        var body = fb.get(1);
        var ast = fb.size() > 2 ? fb.get(2) : null;
        var cloSxp =
            SEXPs.closure(
                forms, body, SEXPs.EMPTY_ENV, ast == null ? null : Attributes.srcref(ast));

        // Note that we don't use `module.getOrCompile` because the inner closure will probably have
        // a unique environment, so we don't even bother trying to cache it outside of the outer
        // closure.
        // Also, we intentionally only compile the baseline version because the inner closure will
        // be optimized along with the outer closure.
        // TODO: infer name
        Closure closure;
        try {
          closure = compileBaselineClosure("", cloSxp, env, module);
        } catch (ClosureCompilerUnsupportedException e) {
          throw failUnsupported("Closure with unsupported body", e);
        }

        pushInsert(new StmtData.MkCls(closure));
      }
      case UMinus(var ast) -> pushInsert(mkUnop(ast, StmtData.UMinus::new));
      case UPlus(var ast) -> pushInsert(mkUnop(ast, StmtData.UPlus::new));
      case Sqrt(var ast) -> pushInsert(mkUnop(ast, StmtData.Sqrt::new));
      case Add(var ast) -> pushInsert(mkBinop(ast, StmtData.Add::new));
      case Sub(var ast) -> pushInsert(mkBinop(ast, StmtData.Sub::new));
      case Mul(var ast) -> pushInsert(mkBinop(ast, StmtData.Mul::new));
      case Div(var ast) -> pushInsert(mkBinop(ast, StmtData.Div::new));
      case Expt(var ast) -> pushInsert(mkBinop(ast, StmtData.Pow::new));
      case Exp(var ast) -> pushInsert(mkUnop(ast, StmtData.Exp::new));
      case Eq(var ast) -> pushInsert(mkBinop(ast, StmtData.Eq::new));
      case Ne(var ast) -> pushInsert(mkBinop(ast, StmtData.Neq::new));
      case Lt(var ast) -> pushInsert(mkBinop(ast, StmtData.Lt::new));
      case Le(var ast) -> pushInsert(mkBinop(ast, StmtData.Lte::new));
      case Ge(var ast) -> pushInsert(mkBinop(ast, StmtData.Gte::new));
      case Gt(var ast) -> pushInsert(mkBinop(ast, StmtData.Gt::new));
      case And(var ast) -> pushInsert(mkBinop(ast, StmtData.LAnd::new));
      case Or(var ast) -> pushInsert(mkBinop(ast, StmtData.LOr::new));
      case Not(var ast) -> pushInsert(mkUnop(ast, StmtData.Not::new));
      case DotsErr() -> insert(new StmtData.Error("'...' used in an incorrect context", env));
      case StartAssign(var name) -> {
        var lhs = cursor.insert(get(name).name(), new StmtData.LdVar(get(name), false, env));
        var rhs = top(RValue.class);
        pushComplexAssign(false, get(name), lhs, rhs);
      }
      case EndAssign(var name) -> {
        var lhs = popComplexAssign(false, get(name));
        insert(new StmtData.StVar(get(name), lhs, env));
      }
      case StartSubset(var ast, var after) ->
          compileStartDispatch(Dispatch.Type.SUBSET, ast, after);
      case DfltSubset() ->
          compileDefaultDispatch(
              Dispatch.Type.SUBSET,
              2,
              4,
              (ast, args) ->
                  switch (args.size()) {
                    case 2 ->
                        new StmtData.Extract1_1D(Optional.of(ast), args.get(0), args.get(1), env);
                    case 3 ->
                        new StmtData.Extract1_2D(
                            Optional.of(ast), args.get(0), args.get(1), args.get(2), env);
                    case 4 ->
                        new StmtData.Extract1_3D(
                            Optional.of(ast),
                            args.get(0),
                            args.get(1),
                            args.get(2),
                            args.get(3),
                            env);
                    default ->
                        throw new UnreachableError("index should've been range-checked above");
                  });
      case StartSubassign(var ast, var after) ->
          compileStartDispatch(Dispatch.Type.SUBASSIGN, ast, after);
      case DfltSubassign() ->
          compileDefaultDispatch(
              Dispatch.Type.SUBASSIGN,
              3,
              5,
              (ast, args) ->
                  switch (args.size()) {
                    case 3 ->
                        new StmtData.Subassign1_1D(
                            Optional.of(ast), args.get(0), args.get(1), args.get(2), env);
                    case 4 ->
                        new StmtData.Subassign1_2D(
                            Optional.of(ast),
                            args.get(0),
                            args.get(1),
                            args.get(2),
                            args.get(3),
                            env);
                    case 5 ->
                        new StmtData.Subassign1_3D(
                            Optional.of(ast),
                            args.get(0),
                            args.get(1),
                            args.get(2),
                            args.get(3),
                            args.get(4),
                            env);
                    default ->
                        throw new UnreachableError("index should've been range-checked above");
                  });
      case StartC(var ast, var after) -> compileStartDispatch(Dispatch.Type.C, ast, after);
      case DfltC() ->
          compileDefaultDispatch(
              Dispatch.Type.C,
              0,
              0,
              (_, _) -> {
                throw new UnreachableError("There isn't a specialized PIR instruction for C");
              });
      case StartSubset2(var ast, var after) ->
          compileStartDispatch(Dispatch.Type.SUBSET2, ast, after);
      case DfltSubset2() ->
          compileDefaultDispatch(
              Dispatch.Type.SUBSET2,
              2,
              3,
              (ast, args) ->
                  switch (args.size()) {
                    case 2 ->
                        new StmtData.Extract2_1D(Optional.of(ast), args.get(0), args.get(1), env);
                    case 3 ->
                        new StmtData.Extract2_2D(
                            Optional.of(ast), args.get(0), args.get(1), args.get(2), env);
                    default ->
                        throw new UnreachableError("index should've been range-checked above");
                  });
      case StartSubassign2(var ast, var after) ->
          compileStartDispatch(Dispatch.Type.SUBASSIGN2, ast, after);
      case DfltSubassign2() ->
          compileDefaultDispatch(
              Dispatch.Type.SUBASSIGN2,
              3,
              4,
              (ast, args) ->
                  switch (args.size()) {
                    case 3 ->
                        new StmtData.Subassign2_1D(
                            Optional.of(ast), args.get(0), args.get(1), args.get(2), env);
                    case 4 ->
                        new StmtData.Subassign2_2D(
                            Optional.of(ast),
                            args.get(0),
                            args.get(1),
                            args.get(2),
                            args.get(3),
                            env);
                    default ->
                        throw new UnreachableError("index should've been range-checked above");
                  });
      case Dollar(var ast, var member) -> {
        var after = cfg.addBB("afterDollar");

        var target = top(RValue.class);
        compileGeneralDispatchCommon(BuiltinId.DOLLAR, false, get(ast), target, null, after);

        var call = popCall();
        if (!call.fun.equals(new Call.Fun.Builtin(BuiltinId.DOLLAR))) {
          throw fail("Dollar: expected a call to $");
        }

        pop(RValue.class);
        pushInsert(
            new StmtData.CallBuiltin(
                Optional.of(get(ast)),
                BuiltinId.DOLLAR,
                ImmutableList.of(target, new Constant(get(member))),
                env));
        cursor.insert(new JumpData.Goto(after));

        moveTo(after);
      }
      case DollarGets(var ast, var member) -> {
        var after = cfg.addBB("afterDollarGets");

        var rhs = pop(RValue.class);
        var target = top(RValue.class);
        compileGeneralDispatchCommon(BuiltinId.DOLLAR_GETS, false, get(ast), target, rhs, after);

        var call = popCall();
        if (!call.fun.equals(new Call.Fun.Builtin(BuiltinId.DOLLAR_GETS))) {
          throw fail("Dollar: expected a call to $");
        }

        pop(RValue.class);
        pushInsert(
            new StmtData.CallBuiltin(
                Optional.of(get(ast)),
                BuiltinId.DOLLAR_GETS,
                ImmutableList.of(target, new Constant(get(member)), rhs),
                env));
        cursor.insert(new JumpData.Goto(after));

        moveTo(after);
      }
      case IsNull() ->
          pushInsert(
              new StmtData.Eq(Optional.empty(), pop(RValue.class), new Constant(SEXPs.NULL), env));
      case IsLogical() -> pushInsert(new StmtData.GnuRIs(IsTypeCheck.LGL, pop(RValue.class)));
      case IsInteger() -> pushInsert(new StmtData.GnuRIs(IsTypeCheck.INT, pop(RValue.class)));
      case IsDouble() -> pushInsert(new StmtData.GnuRIs(IsTypeCheck.REAL, pop(RValue.class)));
      case IsComplex() -> pushInsert(new StmtData.GnuRIs(IsTypeCheck.CPLX, pop(RValue.class)));
      case IsCharacter() -> pushInsert(new StmtData.GnuRIs(IsTypeCheck.STR, pop(RValue.class)));
      case IsSymbol() -> pushInsert(new StmtData.GnuRIs(IsTypeCheck.SYM, pop(RValue.class)));
      case IsObject() -> pushInsert(new StmtData.GnuRIs(IsTypeCheck.NON_OBJECT, pop(RValue.class)));
      case IsNumeric() -> pushInsert(new StmtData.GnuRIs(IsTypeCheck.NUM, pop(RValue.class)));
      case VecSubset(var _) ->
          compileDefaultDispatchN(
              Dispatch.Type.SUBSETN,
              2,
              (ast, args) ->
                  new StmtData.Extract2_1D(Optional.of(ast), args.getFirst(), args.get(1), env));
      case MatSubset(var _) ->
          compileDefaultDispatchN(
              Dispatch.Type.SUBSETN,
              3,
              (ast, args) ->
                  new StmtData.Extract2_2D(
                      Optional.of(ast), args.getFirst(), args.get(1), args.get(2), env));
      case VecSubassign(var _) ->
          compileDefaultDispatchN(
              Dispatch.Type.SUBASSIGNN,
              3,
              (ast, args) ->
                  new StmtData.Subassign2_1D(
                      Optional.of(ast), args.getFirst(), args.get(1), args.get(2), env));
      case MatSubassign(var _) ->
          compileDefaultDispatchN(
              Dispatch.Type.SUBASSIGNN,
              4,
              (ast, args) ->
                  new StmtData.Subassign2_2D(
                      Optional.of(ast),
                      args.getFirst(),
                      args.get(1),
                      args.get(2),
                      args.get(3),
                      env));
      case And1st(var ast, var shortCircuit) -> {
        var shortCircuitBb = bbAt(shortCircuit);
        pushInsert(new StmtData.AsLogical(pop(RValue.class)));
        cursor.insert(
            new JumpData.Branch(get(ast), top(RValue.class), bbAfterCurrent(), shortCircuitBb));
        addPhiInputsForStack(shortCircuitBb);
      }
      case And2nd(var ast) -> {
        pushInsert(new StmtData.AsLogical(pop(RValue.class)));
        pushInsert(mkBinop(ast, StmtData.LAnd::new));
        cursor.insert(new JumpData.Goto(bbAfterCurrent()));
      }
      case Or1st(var ast, var shortCircuit) -> {
        var shortCircuitBb = bbAt(shortCircuit);
        pushInsert(new StmtData.AsLogical(pop(RValue.class)));
        cursor.insert(
            new JumpData.Branch(get(ast), top(RValue.class), shortCircuitBb, bbAfterCurrent()));
        addPhiInputsForStack(shortCircuitBb);
      }
      case Or2nd(var ast) -> {
        pushInsert(new StmtData.AsLogical(pop(RValue.class)));
        pushInsert(mkBinop(ast, StmtData.LOr::new));
        cursor.insert(new JumpData.Goto(bbAfterCurrent()));
      }
      case GetVarMissOk(var name) -> {
        pushInsert(name, new StmtData.LdVar(get(name), true, env));
        pushInsert(new StmtData.Force(pop(RValue.class), compileFrameState(), env));
      }
      case DdValMissOk(var name) -> {
        var ddIndex = get(name).ddNum();
        pushInsert(name, new StmtData.LdDdVal(ddIndex, true, env));
        pushInsert(new StmtData.Force(pop(RValue.class), compileFrameState(), env));
      }
      case Visible() -> insert(new StmtData.Visible());
      case SetVar2(var name) -> insert(new StmtData.StVarSuper(get(name), top(RValue.class), env));
      case StartAssign2(var name) -> {
        // GNU-R has "cells" and stores the assign on the main stack.
        // But we don't have cells, and since we're compiling, we can store the assignment on its
        // own stack.
        var lhs = cursor.insert(get(name).name(), new StmtData.LdVarSuper(get(name), env));
        var rhs = top(RValue.class);
        pushComplexAssign(true, get(name), lhs, rhs);
      }
      case EndAssign2(var name) -> {
        var lhs = popComplexAssign(true, get(name));
        insert(new StmtData.StVarSuper(get(name), lhs, env));
      }
      case SetterCall(var ast, var _) -> {
        // GNU-R has to wrap these call args in evaluated promises depending on the call type,
        // but presumably this is something we abstract. This is also what `valueExpr` is for,
        // which is why it's unused.
        var rhs = pop(RValue.class);
        var lhs = pop(RValue.class);
        prependCallArg(lhs);
        pushCallArg(rhs);
        compileCall(ast);
      }
      case GetterCall(var ast) -> {
        // GNU-R has to wrap this call arg in an evaluated promise depending on the call type,
        // but presumably this is something we abstract.
        prependCallArg(top(RValue.class));
        compileCall(ast);
      }
      case SpecialSwap() -> {
        var value = pop(Node.class);
        var value2 = pop(Node.class);
        push(value);
        push(value2);
      }
      case Dup2nd() -> {
        var value = pop(Node.class);
        var value2 = top(Node.class);
        push(value);
        push(value2);
      }
      case ReturnJmp() -> {
        var retVal = pop(RValue.class);
        assertStacksForReturn();
        cursor.insert(new JumpData.NonLocalReturn(retVal, env));
        setInUnreachableBytecode();
      }
      case Switch(var ast, var namesIdx, var chrLabelsIdx, var numLabelsIdx) -> {
        var names = namesIdx == null ? null : get(namesIdx);
        var chrLabels = chrLabelsIdx == null ? null : get(chrLabelsIdx);
        var numLabels = numLabelsIdx == null ? null : get(numLabelsIdx);

        var value = pop(RValue.class);

        var isVector = cursor.insert(new GnuRIs(IsTypeCheck.SCALAR, value));
        var isVectorBb = cfg.addBB();
        var isNotVectorBb = cfg.addBB("failNotVector");
        cursor.insert(new JumpData.Branch(isVector, isVectorBb, isNotVectorBb));

        // Has one predecessor (no phis) so we can call `cursor.moveToStart` instead of `moveTo`.
        cursor.moveToStart(isNotVectorBb);
        cursor.insert(new StmtData.Error("EXPR must be a length 1 vector", env));
        cursor.insert(new JumpData.Unreachable());

        // Has one predecessor (no phis) so we can call `cursor.moveToStart` instead of `moveTo`.
        cursor.moveToStart(isVectorBb);
        var isFactor = cursor.insert(new GnuRIs(IsTypeCheck.FACTOR, value));
        var isFactorBb = cfg.addBB();
        var isNotFactorBb = cfg.addBB("warnNotFactor");
        cursor.insert(new JumpData.Branch(isFactor, isFactorBb, isNotFactorBb));

        // Has one predecessor (no phis) so we can call `cursor.moveToStart` instead of `moveTo`.
        cursor.moveToStart(isNotFactorBb);
        cursor.insert(
            new StmtData.Warning(
                "EXPR is a \"factor\", treated as integer.\n Consider using 'switch(as.character( * ), ...)' instead."));
        cursor.insert(new JumpData.Goto(isFactorBb));

        // Has two predecessors, but the second defines no values (means no phis), so we can call
        // `cursor.moveToStart` instead of `moveTo`.
        cursor.moveToStart(isFactorBb);
        var isString = cursor.insert(new GnuRIs(IsTypeCheck.STR, value));
        var stringBb = cfg.addBB("switchString");
        var asIntegerBb = cfg.addBB("switchAsInteger");
        cursor.insert(new JumpData.Branch(isString, stringBb, asIntegerBb));

        // Has one predecessor (no phis) so we can call `cursor.moveToStart` instead of `moveTo`.
        cursor.moveToStart(stringBb);
        if (names == null) {
          if (numLabels == null) {
            cursor.insert(new StmtData.Error("bad numeric 'switch' offsets", env));
            cursor.insert(new JumpData.Unreachable());
          } else if (numLabels.isScalar()) {
            cursor.insert(new StmtData.Warning("'switch' with no alternatives"));
            cursor.insert(new JumpData.Goto(bbAt(new BcLabel(numLabels.get(0)))));
          } else {
            cursor.insert(
                new StmtData.Error(
                    "numeric EXPR required for 'switch' without named alternatives", env));
            cursor.insert(new JumpData.Unreachable());
          }
        } else {
          if (chrLabels == null) {
            cursor.insert(new StmtData.Error("bad character 'switch' offsets", env));
            cursor.insert(new JumpData.Unreachable());
          } else if (names.size() != chrLabels.size()) {
            cursor.insert(new StmtData.Error("bad 'switch' names", env));
            cursor.insert(new JumpData.Unreachable());
          } else {
            for (var i = 0; i < chrLabels.size() - 1; i++) {
              var name = names.get(i);
              var ifMatch = bbAt(new BcLabel(chrLabels.get(i)));
              var cond =
                  cursor.insert(
                      new StmtData.Eq(Optional.of(get(ast)), value, Constant.string(name), env));
              var prev = cursor.bb();
              cursor.insert(next -> new JumpData.Branch(cond, ifMatch, next));
              addPhiInputsForStack(ifMatch, prev);
            }
            // `switch` just goes to the last label regardless of whether it matches.
            var lastBb = bbAt(new BcLabel(chrLabels.last()));
            cursor.insert(new JumpData.Goto(lastBb));
            addPhiInputsForStack(lastBb);
          }
        }

        // Has one predecessor (no phis) so we can call `cursor.moveToStart` instead of `moveTo`.
        cursor.moveToStart(asIntegerBb);
        if (numLabels == null) {
          cursor.insert(new StmtData.Error("bad numeric 'switch' offsets", env));
          cursor.insert(new JumpData.Unreachable());
        } else if (numLabels.isScalar()) {
          cursor.insert(new StmtData.Warning("'switch' with no alternatives"));
          cursor.insert(new JumpData.Goto(bbAt(new BcLabel(numLabels.get(0)))));
        } else {
          var asInteger = cursor.insert(new StmtData.AsSwitchIdx(value));
          for (var i = 0; i < numLabels.size() - 1; i++) {
            var ifMatch = bbAt(new BcLabel(numLabels.get(i)));
            var cond =
                cursor.insert(
                    new StmtData.Eq(Optional.of(get(ast)), asInteger, Constant.integer(i), env));
            var prev = cursor.bb();
            cursor.insert(next -> new JumpData.Branch(cond, ifMatch, next));
            addPhiInputsForStack(ifMatch, prev);
          }
          // `switch` just goes to the last label regardless of whether it matches.
          var lastBb = bbAt(new BcLabel(numLabels.last()));
          cursor.insert(new JumpData.Goto(lastBb));
          addPhiInputsForStack(lastBb);
        }

        // The `switch` executes a GOTO on all branches, so the following code is unreachable unless
        // there's a label immediately after (which is probably guaranteed).
        setInUnreachableBytecode();
      }
      case StartSubsetN(var ast, var after) ->
          compileStartDispatch(Dispatch.Type.SUBSETN, ast, after);
      case StartSubassignN(var ast, var after) ->
          compileStartDispatch(Dispatch.Type.SUBASSIGNN, ast, after);
      case VecSubset2(var _) ->
          compileDefaultDispatchN(
              Dispatch.Type.SUBSET2N,
              2,
              (ast, args) ->
                  new StmtData.Extract2_1D(Optional.of(ast), args.getFirst(), args.get(1), env));
      case MatSubset2(var _) ->
          compileDefaultDispatchN(
              Dispatch.Type.SUBSET2N,
              3,
              (ast, args) ->
                  new StmtData.Extract2_2D(
                      Optional.of(ast), args.getFirst(), args.get(1), args.get(2), env));
      case VecSubassign2(var _) ->
          compileDefaultDispatchN(
              Dispatch.Type.SUBASSIGN2N,
              3,
              (ast, args) ->
                  new StmtData.Subassign2_1D(
                      Optional.of(ast), args.getFirst(), args.get(1), args.get(2), env));
      case MatSubassign2(var _) ->
          compileDefaultDispatchN(
              Dispatch.Type.SUBASSIGN2N,
              4,
              (ast, args) ->
                  new StmtData.Subassign2_2D(
                      Optional.of(ast),
                      args.getFirst(),
                      args.get(1),
                      args.get(2),
                      args.get(3),
                      env));
      case StartSubset2N(var ast, var after) ->
          compileStartDispatch(Dispatch.Type.SUBSET2N, ast, after);
      case StartSubassign2N(var ast, var after) ->
          compileStartDispatch(Dispatch.Type.SUBASSIGN2N, ast, after);
      case SubsetN(var _, var n) -> compileDefaultDispatchN(Dispatch.Type.SUBSETN, n + 1);
      case Subset2N(var _, var n) -> compileDefaultDispatchN(Dispatch.Type.SUBSET2N, n + 1);
      case SubassignN(var _, var n) -> compileDefaultDispatchN(Dispatch.Type.SUBASSIGNN, n + 2);
      case Subassign2N(var _, var n) -> compileDefaultDispatchN(Dispatch.Type.SUBASSIGN2N, n + 2);
      case Log(var ast) -> pushInsert(mkUnop(ast, StmtData.Log::new));
      case LogBase(var ast) -> pushInsert(mkBinop(ast, StmtData.LogBase::new));
      case Math1(var ast, var funId) ->
          pushInsert(new StmtData.Math1(Optional.of(get(ast)), funId, pop(RValue.class), env));
      case DotCall(var ast, var numArgs) -> {
        if (stack.size() < numArgs + 1) {
          throw fail("stack underflow");
        }
        var funAndArgs = stack.subList(stack.size() - numArgs - 1, stack.size());
        var args =
            funAndArgs.stream().map(a -> (RValue) a).collect(ImmutableList.toImmutableList());
        funAndArgs.clear();

        pushInsert(new StmtData.CallBuiltin(get(ast), BuiltinId.DOT_CALL, args, env));
      }
      case Colon(var ast) -> pushInsert(mkBinop(ast, StmtData.Colon::new));
      case SeqAlong(var ast) ->
          pushInsert(
              new StmtData.CallBuiltin(
                  Optional.of(get(ast)),
                  BuiltinId.SEQ_ALONG,
                  ImmutableList.of(pop(RValue.class)),
                  env));
      case SeqLen(var ast) ->
          pushInsert(
              new StmtData.CallBuiltin(
                  Optional.of(get(ast)),
                  BuiltinId.SEQ_LEN,
                  ImmutableList.of(pop(RValue.class)),
                  env));
      case BaseGuard(var exprIdx, var after) -> {
        // PIR apparently just ignores the guards (`rir2pir.cpp:341`), but we can handle here.
        var expr = get(exprIdx);
        var fun = (RegSymSXP) expr.fun();
        var sym = cursor.insert(fun.name(), new StmtData.LdFun(fun, env));
        var base = cursor.insert("base." + fun.name(), new StmtData.LdFun(fun, StaticEnv.BASE));
        var guard = cursor.insert(new StmtData.Eq(Optional.of(expr), sym, base, env));

        var safeBb = cfg.addBB("baseGuardSafe");
        var fallbackBb = cfg.addBB("baseGuardFail");
        var afterBb = bbAt(after);
        cursor.insert(new JumpData.Branch(guard, safeBb, fallbackBb));

        // Has one predecessor (no phis) so we can call `cursor.moveToStart` instead of `moveTo`.
        cursor.moveToStart(fallbackBb);
        // Compile a call.
        // Also, pass `Constant`s containing symbols and language objects to arguments,
        // expecting them to be `eval`ed, like in `CallSpecial`.
        var argNames =
            expr.args().names().stream()
                .map(Optional::ofNullable)
                .collect(ImmutableList.toImmutableList());
        var args =
            expr.args().values().stream()
                .map(v -> (RValue) new Constant(v))
                .collect(ImmutableList.toImmutableList());
        var fs = compileFrameState();
        pushInsert(
            argNames.stream().anyMatch(Optional::isPresent)
                ? new StmtData.NamedCall(expr, sym, argNames, args, env, fs)
                : new StmtData.NamelessCall(expr, sym, args, env, fs));
        cursor.insert(new JumpData.Goto(afterBb));
        addPhiInputsForStack(afterBb);
        pop(RValue.class); // the `CallBuiltin` pushed above.

        // Has one predecessor (no phis) so we can call `cursor.moveToStart` instead of `moveTo`.
        cursor.moveToStart(safeBb);
      }
      case IncLnk(), DecLnk(), DeclnkN(var _), IncLnkStk(), DecLnkStk() -> {}
    }
  }

  @FunctionalInterface
  private interface MkUnopFn {
    StmtData.RValue_ make(Optional<LangSXP> ast, RValue value, @IsEnv RValue env);
  }

  @FunctionalInterface
  private interface MkBinopFn {
    StmtData.RValue_ make(Optional<LangSXP> ast, RValue lhs, RValue rhs, @IsEnv RValue env);
  }

  private StmtData.RValue_ mkUnop(ConstPool.Idx<LangSXP> ast, MkUnopFn unop) {
    return unop.make(Optional.of(get(ast)), pop(RValue.class), env);
  }

  private StmtData.RValue_ mkBinop(ConstPool.Idx<LangSXP> ast, MkBinopFn binop) {
    var rhs = pop(RValue.class);
    var lhs = pop(RValue.class);
    return binop.make(Optional.of(get(ast)), lhs, rhs, env);
  }

  /**
   * End the previously-compiled for loop instruction (the latest {@link #pushWhileOrRepeatLoop(BB,
   * BB)} or {@link #pushForLoop(BB, BB, BB)}): pop its data, assert that the loop is of the correct
   * type, and that the cursor is right after its end.
   */
  private void compileEndLoop(LoopType type) {
    var loop = popLoop();
    if (loop.type != type) {
      throw fail("expected a " + type + " loop");
    }
    if ((cursor.bb() != loop.end
            // If there is a `break` instruction in a for loop, there will be a label at `EndFor`,
            // so `loop.end` will only consist of a `Goto` to the "real" end BB. This is allowed,
            // and this long inverted AND-chain checks if it's the case.
            && !(loop.end.stmts().isEmpty()
                && loop.end.jumpData() instanceof JumpData.Goto(var loopEndGoto)
                && loopEndGoto == cursor.bb()))
        || cursor.stmtIdx() != 0) {
      throw fail("compileEndLoop: expected to be at start of end BB " + loop.end.id());
    }
  }

  private void compileStartDispatch(Dispatch.Type type, ConstPool.Idx<LangSXP> ast, BcLabel after) {
    compileStartDispatch(type, get(ast), bbAt(after));
  }

  private void compileStartDispatch(Dispatch.Type type, LangSXP ast, BB after) {
    var rhs = type.hasRhs ? pop(RValue.class) : null;
    var target = top(RValue.class);
    compileGeneralDispatchCommon(type.builtin, type.isNForm, ast, target, rhs, after);
    if (rhs != null) {
      push(rhs);
    }

    if (!type.isNForm) {
      pushCallArg(target);
      if (!ast.args().isEmpty() && ast.argName(0) != null) {
        setNameOfLastCallArg(Objects.requireNonNull(ast.argName(0)).name());
      }
    }

    pushDispatch(type, ast, after);
  }

  private void compileGeneralDispatchCommon(
      BuiltinId fun, boolean isNForm, LangSXP ast, RValue target, @Nullable RValue rhs, BB after) {
    var isNotObject = cursor.insert(new StmtData.GnuRIs(IsTypeCheck.NON_OBJECT, target));
    var nonObjectBb = cfg.addBB("dispatchNonObject");
    var objectBb = cfg.addBB("dispatchObject");
    cursor.insert(new JumpData.Branch(isNotObject, nonObjectBb, objectBb));

    // Has one predecessor (no phis) so we can call `cursor.moveToStart` instead of `moveTo`.
    cursor.moveToStart(objectBb);
    var target1 = pop(RValue.class);
    assert target == target1
        : "`compileGeneralDispatchCommon` only called with top-of-stack as `target`";
    var dispatch = cursor.insert(new StmtData.TryDispatchBuiltin_(ast, fun, target, rhs, env));
    cursor.insert(next -> new JumpData.Branch(dispatch.dispatched(), next, nonObjectBb));
    push(dispatch.value());
    cursor.insert(new JumpData.Goto(after));
    addPhiInputsForStack(after);
    pop(RValue.class); // the `CallBuiltin` pushed above.

    // Has one predecessor (no phis) so we can call `cursor.moveToStart` instead of `moveTo`.
    cursor.moveToStart(nonObjectBb);
    push(target);

    if (!isNForm) {
      pushCall(fun);
    }
  }

  private void compileDefaultDispatch(
      Dispatch.Type type,
      int minNumArgs,
      int maxNumArgs,
      BiFunction<LangSXP, List<RValue>, StmtData.RValue_> dispatchInstrData) {
    assert !type.isNForm : "use compileDefaultDispatchN for `...N` bytecodes";

    if (type.hasRhs) {
      pushCallArg(pop(RValue.class));
    }

    var dispatch = popDispatch(type);
    var call = popCall();

    pop(RValue.class);

    var argValues = Lists.map(call.args, Call.Arg::value);
    if (call.args.stream()
            .anyMatch(
                arg -> arg.name() != null || arg.value.equals(new Constant(SEXPs.DOTS_SYMBOL)))
        || call.args.size() < minNumArgs
        || call.args.size() > maxNumArgs) {
      // Slowcase, for when we don't have a specialized instruction.
      pushInsert(
          new StmtData.CallSafeBuiltin(
              dispatch.ast, type.builtin, ImmutableList.copyOf(argValues), env));
    } else {
      // Instead of compiling a call, insert our specialized instruction with the call arguments.
      pushInsert(dispatchInstrData.apply(dispatch.ast, argValues));
    }

    cursor.insert(new JumpData.Goto(bbAfterCurrent()));
    if (bbAfterCurrent() != dispatch.after) {
      throw fail("expected to be immediately before `after` BB " + dispatch.after.id());
    }
  }

  private void compileDefaultDispatchN(Dispatch.Type type, int numArgs) {
    compileDefaultDispatchN(type, numArgs, null);
  }

  private void compileDefaultDispatchN(
      Dispatch.Type type,
      int numArgs,
      @Nullable BiFunction<LangSXP, List<RValue>, StmtData.RValue_> dispatchInstrData) {
    assert type.isNForm : "use compileDefaultDispatch for non-`...N` bytecodes";

    var dispatch = popDispatch(type);
    var uncastedArgs = stack.subList(stack.size() - numArgs, stack.size());
    for (var arg : uncastedArgs) {
      if (!(arg instanceof RValue)) {
        throw fail(
            "expected RValue on stack for `compileDefaultDispatchN("
                + type
                + ", "
                + numArgs
                + ", ...)`");
      }
    }
    @SuppressWarnings("unchecked")
    var args = (List<RValue>) (List<?>) uncastedArgs;

    // Unlike `compileDefaultDispatch`, there's no call, instead we take the args from the stack.
    // Like `compileDefaultDispatch`, there's either a specialized instruction or a fallback call,
    // although in this the number of arguments is known.
    var result =
        dispatchInstrData == null
            ? new StmtData.CallSafeBuiltin(
                dispatch.ast, type.builtin, ImmutableList.copyOf(args), env)
            : dispatchInstrData.apply(dispatch.ast, args);
    args.clear();
    pushInsert(result);

    cursor.insert(new JumpData.Goto(bbAfterCurrent()));
    if (bbAfterCurrent() != dispatch.after) {
      throw fail("expected to be immediately before `after` BB " + dispatch.after.id());
    }
  }

  private void compileCall(ConstPool.Idx<LangSXP> ast) {
    compileCall(popCall(), get(ast));
  }

  private void compileCall(Call call, LangSXP ast) {
    var names =
        call.args.stream()
            .map(arg -> Optional.ofNullable(arg.name))
            .collect(ImmutableList.toImmutableList());
    var args = call.args.stream().map(arg -> arg.value).collect(ImmutableList.toImmutableList());
    var isNamed = call.args.stream().anyMatch(arg -> arg.name() != null);
    var callInstr =
        cursor.insert(
            switch (call.fun) {
              case Call.Fun.Value(var fun) ->
                  isNamed
                      ? new StmtData.NamedCall(ast, fun, names, args, env, compileFrameState())
                      : new NamelessCall(ast, fun, args, env, compileFrameState());
              case Call.Fun.Builtin(var fun) ->
                  // Even if `isNamed` is true, R just ignores the names.
                  new StmtData.CallBuiltin(ast, fun, args, env);
            });
    push(callInstr);

    // Special-case due to weird and possibly buggy (GNU-R) bytecode compiler behavior:
    // when the bytecode compiler compiles a `switch` with a missing label, it inserts a branch that
    // ends with `stop("empty alternative in numeric switch")`. This call pushes an extra value
    // (call return) onto the stack. When interpreted, the call to `stop` usually\* exits the
    // function, so the stack no longer matters. But when we compile to IR, we still expect the
    // stack to be balanced, and this call return is unaccounted for. Therefore, we have to special-
    // case, and we do so by checking a) the exact function and b) we're about to go to another
    // label without any phis. If both these conditions are met, we're guaranteed to be in this
    // special-case (unless there's another compiler bug causing stack imbalances, and even then,
    // very unlikely), so we pop the result of the call to re-balance the stack.
    //
    // \* If `stop` is overridden in the function, the bytecode compiler will use the overridden
    // call, which is different than the AST interpreter which errors regardless. This and the fact
    // that the stack now has an extra value until the function returns, are why it may be a bug.
    if (callInstr.data() instanceof StmtData.NamelessCall n
        && n.fun() instanceof Stmt f
        && f.data() instanceof StmtData.LdFun(var name, var _)
        && name.name().equals("stop")
        && n.args().size() == 1
        && n.args().getFirst().equals(Constant.string("empty alternative in numeric switch"))
        && hasBbAfterCurrent()
        && bbAfterCurrent().phis().size() == stack.size() - 1) {
      pop(RValue.class);
    }
  }

  private FrameState compileFrameState() {
    return cursor.insert(
        new FrameState_(new BcPosition(bcPos), isPromise, ImmutableList.copyOf(stack), env));
  }

  // endregion main compile functions: compile individual things

  // region `moveTo` and phis

  /**
   * Move the cursor to the basic block <i>and</i> replace all stack arguments with its phis.
   *
   * <p>Replacing the stack with phis is part of converting a stack-based bytecode to SSA-based IR.
   * Since actual interpretation we may encounter this block from different predecessors, it needs
   * phi nodes in order to preserve the SSA invariant "each variable is assigned exactly once".
   * Although the block may have only one predecessor, in which case phi nodes are unnecessary, we
   * start by assigning phi nodes to every block, and then the single-input phis in a cleanup phase
   * later.
   *
   * <p>If the current block is a predecessor to the given block, the given block's stack will have
   * a phi for each stack node in the current block, and one of the inputs will be said node (see
   * {@link #addPhiInputsForStack(BB)}). We call {@link #addPhiInputsForStack(BB)} here for
   * convenience, although it makes this method have 2 functions (add phis if the next block is a
   * successor, and move to it). Otherwise, we don't know what its stack will look like from the
   * current block, but we can rely on the invariant that it already had phis added from a different
   * block, so we use the phis that were already added (if it didn't have phis added from a
   * different block, we throw {@link AssertionError}).
   */
  private void moveTo(BB bb) {
    // First ensure the block has phis, and add inputs from this block if necessary.
    if (bb.predecessors().contains(cursor.bb())) {
      // Make sure this block has phis representing the arguments currently on the stack, then
      // replace
      // the stack with those phis (this is how to convert a stack-based bytecode to an SSA-form
      // IR).
      addPhiInputsForStack(bb);
    } else {
      // This won't work unless we've already added phis from another BB
      assert bbsWithPhis.contains(bb)
          : "tried to move to BB "
              + bb.id()
              + " which isn't a successor and didn't have phis added yet, so we don't know what its stack looks like";
    }

    // Then replace the stack with those phis.
    stack.clear();
    stack.addAll(bb.phis());

    // Finally, actually move the cursor to the BB.
    cursor.moveToStart(bb);
  }

  /**
   * Add inputs to the given block's phis for the stack at the current block.
   *
   * <p>The currently block is required to be one of the given block's predecessors. This needs to
   * be called for every jump to a block returned by {@link #bbAt(BcLabel)} in order to uphold the
   * SSA invariant "each variable is assigned exactly once"; see {@link #moveTo(BB)} for more
   * explanation.
   */
  private void addPhiInputsForStack(BB bb) {
    addPhiInputsForStack(bb, cursor.bb());
  }

  /**
   * Add inputs to the given block's phis for the stack at {@code incoming}.
   *
   * <p>This is the same as {@link #addPhiInputsForStack(BB)} except uses {@code incoming} instead
   * of the current block.
   */
  private void addPhiInputsForStack(BB bb, BB incoming) {
    // Ensure the BB has phis for each stack value.
    if (bbsWithPhis.add(bb)) {
      // Add phis for all of the nodes on the stack.
      for (var node : stack) {
        bb.addPhi(node.getClass());
      }
    } else {
      // Already added phis,
      // but sanity-check that they're still same type and amount as the nodes on the stack.
      var phis = bb.phis().iterator();
      for (var i = 0; i < stack.size(); i++) {
        if (!phis.hasNext()) {
          throw fail(
              "BB stack mismatch: "
                  + bb.id()
                  + " has too few phis; expected "
                  + stack.size()
                  + " but got "
                  + i
                  + "\nIncoming stack: ["
                  + Strings.join(", ", Node::id, stack)
                  + "]");
        }
        var phi = phis.next();
        if (!phi.nodeClass().isInstance(stack.get(i))) {
          throw fail(
              "BB stack mismatch: "
                  + bb.id()
                  + " has a phi of type "
                  + phi.getClass()
                  + " at index "
                  + i
                  + " but expected "
                  + stack.get(i).getClass()
                  + "\nIncoming stack: ["
                  + Strings.join(", ", Node::id, stack)
                  + "]");
        }
      }
      if (phis.hasNext()) {
        var i = stack.size();
        while (phis.hasNext()) {
          phis.next();
          i++;
        }
        throw fail(
            "BB stack mismatch: "
                + bb.id()
                + " has too many phis; expected "
                + stack.size()
                + " but got "
                + i
                + "\nIncoming stack: ["
                + Strings.join(", ", Node::id, stack)
                + "]");
      }
    }

    // Add an input to each phi, for the corresponding stack value and the current BB.
    var phis = bb.phis().iterator();
    for (var node : stack) {
      @SuppressWarnings("unchecked")
      var phi = (Phi<Node>) phis.next();
      phi.setInput(incoming, node);
    }
  }

  // endregion `moveTo` and phis

  // region statement and jump insertions

  /** Insert a statement that doesn't produce a value. */
  private void insert(StmtData.Void data) {
    cursor.insert(data);
  }

  /**
   * Insert a statement that produces a value, and push it onto the "virtual stack" via {@link
   * #push(Node)}.
   *
   * <p>Most bytecode instructions that produce values, such as operations, are compiled into IR
   * instructions that are added this way.
   */
  private void pushInsert(StmtData.RValue_ data) {
    push(cursor.insert(data));
  }

  /** {@link #pushInsert(RValue_)} but gives the instruction the name of the symbol. */
  private void pushInsert(ConstPool.Idx<RegSymSXP> name, StmtData.RValue_ data) {
    push(cursor.insert(get(name).name(), data));
  }

  /**
   * Insert a statement that produces a value, and begin a call with it as the function via {@link
   * #pushCall(RValue)}.
   */
  private void pushCallFunInsert(StmtData.RValue_ data) {
    pushCall(cursor.insert(data));
  }

  /** {@link #pushCallFunInsert(RValue_)} but gives the instruction the name of the symbol. */
  private void pushCallFunInsert(ConstPool.Idx<RegSymSXP> name, StmtData.RValue_ data) {
    pushCall(cursor.insert(get(name).name(), data));
  }

  /**
   * Insert a statement that produces a value, and add it to the current call arguments via {@link
   * #pushCallArg(RValue)}.
   */
  private void pushCallArgInsert(StmtData.RValue_ data) {
    pushCallArg(cursor.insert(data));
  }

  /** {@link #pushCallArgInsert(RValue_)} but gives the instruction a name. */
  private void pushCallArgInsert(String name, StmtData.RValue_ data) {
    pushCallArg(cursor.insert(name, data));
  }

  // endregion statement and jump insertions

  // region inter-instruction data

  /**
   * Mark the following bytecode until the next label as unreachable.
   *
   * <p>This means we skip generating IR, since it may underflow the stack (and because we'd
   * optimize out that IR later anyways).
   */
  private void setInUnreachableBytecode() {
    assert !isInUnreachableBytecode : "already set in unreachable bytecode";
    isInUnreachableBytecode = true;
  }

  // - Since we're converting from stack-based, temporal bytecode to SSA form, we use the virtual
  //   stacks to keep track of data from previous instructions that affects future instructions
  //   (specifically, that data is what's "pushed", when the future instructions use the data it's
  //   read, and when the data no longer affects further future instructions it's popped).

  /**
   * Assert that all stacks are empty, unless in a loop.
   *
   * <p>If we return in a loop, the loop stack won't be empty. Additionally, if we return in a for
   * loop, the node stack will contain the loop index.
   *
   * <p>Otherwise, the bytecode compiler pops everything before returning, so the stack should be
   * empty. And especially so when we implicitly return at the end of a function.
   */
  private void assertStacksForReturn() {
    if (loopStack.isEmpty()) {
      assertStacksAreEmpty();
    }
  }

  /** Assert that all stacks are empty, for when the function has ended. */
  private void assertStacksAreEmpty() {
    require(
        stack.isEmpty(),
        () ->
            "stack isn't empty at end of function: [" + Strings.join(", ", Node::id, stack) + "]");
    require(
        loopStack.isEmpty(),
        () -> "loop stack isn't empty at end of function: [" + Strings.join(", ", loopStack) + "]");
    require(
        complexAssignStack.isEmpty(),
        () ->
            "complex assign stack isn't empty at end of function: ["
                + Strings.join(", ", complexAssignStack)
                + "]");
    require(
        callStack.isEmpty(),
        () -> "call stack isn't empty at end of function: [" + Strings.join(", ", callStack) + "]");
    require(
        dispatchStack.isEmpty(),
        () ->
            "dispatch stack isn't empty at end of function: ["
                + Strings.join(", ", dispatchStack)
                + "]");
  }

  /**
   * Push a node onto the "virtual stack" so that the next call to {@link #pop(Class)} or {@link
   * #top(Class)} will return it.
   *
   * <p>The bytecode is stack-based and IR is SSA-form. To convert properly, when the compiler
   * encounters {@code BcInstr.Ld...} and other instructions that push real values onto the
   * interpreter stack, it pushes the node that stores their abstract value ({@link Node}) onto the
   * "virtual stack"; and when the compiler encounters {@link BcInstr.Pop} and other instructions
   * that read/pop real values from the interpreter stack, it reads/pops the corresponding abstract
   * values from the "virtual stack" and passes them as arguments to the IR instructions those
   * bytecode instructions that pop values compile into.
   *
   * <p>Also, stack nodes are replaced by phi nodes whenever the compiler moves to a new basic
   * block: see {@link #moveTo(BB)} for details.
   */
  private void push(Node value) {
    stack.add(value);
  }

  /**
   * Pop a node from the "virtual stack" that was pushed by the last call to {@link #push(Node)}.
   *
   * <p>The bytecode is stack-based and IR is SSA-form, see {@link #push(Node)} for more
   * explanation.
   */
  private <N extends Node> N pop(Class<N> clazz) {
    require(!stack.isEmpty(), () -> "node stack underflow");
    return clazz.cast(stack.removeLast());
  }

  /**
   * Get the node on the top of the "virtual stack" that was pushed by the last call to {@link
   * #push(Node)}, without popping.
   *
   * <p>The bytecode is stack-based and IR is SSA-form, see {@link #push(Node)} for more
   * explanation.
   */
  private <N extends Node> N top(Class<N> clazz) {
    require(!stack.isEmpty(), () -> "no node on stack");
    return clazz.cast(stack.getLast());
  }

  /**
   * Add data that will be referenced by later for loop instructions (via {@link #topLoop()} and
   * {@link #popLoop()}.
   */
  private void pushWhileOrRepeatLoop(BB next, BB end) {
    loopStack.add(new Loop(LoopType.WHILE_OR_REPEAT, null, next, end));
  }

  /**
   * Add data that will be referenced by later for loop instructions (via {@link #topLoop()} and
   * {@link #popLoop()}.
   */
  private void pushForLoop(BB forBody, BB step, BB end) {
    loopStack.add(new Loop(LoopType.FOR, forBody, step, end));
  }

  /**
   * Whether we're currently compiling a loop i.e. there's a loop on the loop stack.
   *
   * <p>"Local" because promises and `eval` are weird and can reference the loop of the environment
   * they are provided, which is a long-jump in GNU-R. We don't currently support compiling this.
   */
  private boolean inLocalLoop() {
    return !loopStack.isEmpty();
  }

  /**
   * Reference data from a previously-compiled for loop instruction (the latest {@link
   * #pushWhileOrRepeatLoop(BB, BB)} or {@link #pushForLoop(BB, BB, BB)}).
   */
  private Loop topLoop() {
    require(!loopStack.isEmpty(), () -> "no for loop on stack");
    return loopStack.getLast();
  }

  /**
   * Reference data from a previously-compiled for loop instruction (the latest {@link
   * #pushWhileOrRepeatLoop(BB, BB)} or {@link #pushForLoop(BB, BB, BB)}), and remove it.
   */
  private Loop popLoop() {
    require(!loopStack.isEmpty(), () -> "for loop stack underflow");
    return loopStack.removeLast();
  }

  /**
   * Add data for {@code StartAssign(2)?} that will be referenced by {@code EndAssign(2)?} (via
   * {@link #popComplexAssign(boolean, RegSymSXP)}).
   *
   * <p>GNU-R stores the complex assign values on the main stack, but we also store them on their
   * own stack for understandability at the cost of negligible performance (same with calls). GNU-R
   * also stores a "cell" for the lhs value, which has a flag to determine if its in an assignment,
   * for ref-counting purposes. TODO how we handle ref-counting because it will probably be much
   * different, but we will probably abstract it so that we don't have to do anything here even in
   * the future.
   */
  private void pushComplexAssign(boolean isSuper, RegSymSXP name, RValue lhs, RValue rhs) {
    complexAssignStack.add(new ComplexAssign(isSuper, name, rhs));
    push(lhs);
    push(rhs);
  }

  /**
   * Get the current complex assign (the one that was pushed by the last call to {@link
   * #pushComplexAssign(boolean, RegSymSXP, RValue, RValue)}), assert that the given {@code isSuper}
   * and {@code name} match its own, and remove it from the stack.
   */
  private RValue popComplexAssign(boolean isSuper, RegSymSXP name) {
    require(!complexAssignStack.isEmpty(), () -> "complex assign stack underflow");
    var assign = complexAssignStack.removeLast();

    if (!name.equals(assign.name)) {
      throw fail(
          "complex assign stack mismatch: expected "
              + (isSuper ? "super" : "regular")
              + " assign for "
              + name
              + " but got "
              + (assign.isSuper ? "super" : "regular")
              + " assign for "
              + assign.name);
    }
    if (isSuper != assign.isSuper) {
      throw fail(
          "complex assign stack mismatch: expected "
              + (isSuper ? "super" : "regular")
              + " assign, but stack has "
              + (assign.isSuper ? "super" : "regular")
              + " assign");
    }

    return pop(RValue.class);
  }

  /** Begin a new call of the given function (abstract value). */
  private void pushCall(RValue fun) {
    callStack.add(new Call(new Call.Fun.Value(fun)));
  }

  /** Begin a new call of the given function (statically known builtin). */
  private void pushCall(BuiltinId fun) {
    callStack.add(new Call(new Call.Fun.Builtin(fun)));
  }

  /**
   * Get the current call (the one that was pushed by the last call to {@link #pushCall(RValue)}).
   */
  private Call topCall() {
    require(!callStack.isEmpty(), () -> "no call on stack");
    return callStack.getLast();
  }

  /**
   * Pop the current call (the one that was pushed by the last call to {@link #pushCall(RValue)}).
   */
  private Call popCall() {
    require(!callStack.isEmpty(), () -> "call stack underflow");
    return callStack.removeLast();
  }

  /**
   * Prepend a value to the current call stack ({@link #topCall()}).
   *
   * <p>This is required for {@link GetterCall} and {@link SetterCall}, since the {@code lhs} of the
   * complex assignment is prepended in the GNU-R. See also {@link #pushCallArg(RValue)}, which
   * appends the argument.
   */
  private void prependCallArg(RValue value) {
    topCall().args.addFirst(new Call.Arg(value));
  }

  /**
   * Push a value onto the current call stack ({@link #topCall()}).
   *
   * <p>GNU-R maintains a the call function and arguments list on the stack, but we store them
   * separately. Either way generates the same IR, but this way makes errors clearer, e.g.
   * forgetting to push a call raises a specific error earlier instead of a stack mismatch later.
   */
  private void pushCallArg(RValue value) {
    topCall().args.add(new Call.Arg(value));
  }

  /**
   * Push the missing value onto the "call arguments list".
   *
   * @see #pushCallArg(RValue)
   */
  private void pushMissingCallArg() {
    topCall().args.add(Call.Arg.missing());
  }

  /**
   * Set the name of the last argument on the "call arguments list".
   *
   * @see #pushCallArg(RValue)
   */
  private void setNameOfLastCallArg(String name) {
    var args = topCall().args;
    var last = args.removeLast();
    args.add(new Call.Arg(last.value(), name));
  }

  /**
   * Add data for a "start dispatch" bytecode instruction that will be referenced by later "default
   * dispatch" instruction (via {@link #popDispatch(Dispatch.Type)}.
   */
  private void pushDispatch(Dispatch.Type type, LangSXP ast, BB after) {
    dispatchStack.add(new Dispatch(type, ast, after));
  }

  /**
   * Reference data from a previously-compiled "start dispatch" bytecode instruction (via {@link
   * #pushDispatch(Dispatch.Type, LangSXP, BB)}), and pop it.
   */
  private Dispatch popDispatch(Dispatch.Type type) {
    require(!dispatchStack.isEmpty(), () -> "dispatch stack underflow");
    var dispatch = dispatchStack.removeLast();
    if (dispatch.type != type) {
      throw fail("dispatch stack mismatch: expected " + type + " but got " + dispatch.type);
    }
    return dispatch;
  }

  // endregion inter-instruction data

  // region misc

  /** Get the {@link SEXP} in the constant pool corresponding to the given index. */
  private <S extends SEXP> S get(ConstPool.Idx<S> idx) {
    return bc.consts().get(idx);
  }

  private void require(boolean condition, Supplier<String> message) {
    if (!condition) {
      throw fail(message.get());
    }
  }

  private CFGCompilerUnsupportedBcException failUnsupported(String message) {
    return failUnsupported(message, null);
  }

  private CFGCompilerUnsupportedBcException failUnsupported(
      String message, @Nullable ClosureCompilerUnsupportedException cause) {
    return new CFGCompilerUnsupportedBcException(message, bc, bcPos, cursor, cause);
  }

  private CFGCompilerException fail(String message) {
    return new CFGCompilerException(message, bc, bcPos, cursor);
  }
  // endregion misc
}
