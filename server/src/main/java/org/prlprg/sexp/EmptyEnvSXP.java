package org.prlprg.sexp;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.UnmodifiableView;
import org.prlprg.parseprint.Printer;
import org.prlprg.util.Pair;

public final class EmptyEnvSXP implements StaticEnvSXP {
  static final EmptyEnvSXP INSTANCE = new EmptyEnvSXP();

  private EmptyEnvSXP() {}

  /** Empty environment has no parent. It is an error to call this method. */
  @Override
  public EmptyEnvSXP parent() {
    // Technically the empty environment's parent is itself. If we actually need to call this for
    // some reason we should change the implementation so that it returns itself.
    throw new UnsupportedOperationException("the empty environment has no parent");
  }

  @Override
  public void setParent(EnvSXP parent) {
    // Technically the empty environment's parent is itself. If we actually need to call this for
    // some reason we should change the implementation so that it allows setting to itself.
    throw new UnsupportedOperationException("the empty environment has no parent");
  }

  @Override
  public void setParent(StaticEnvSXP parent) {
    setParent((EnvSXP) parent);
  }

  @Override
  public Optional<SEXP> get(String name) {
    return Optional.empty();
  }

  @Override
  public void set(String name, SEXP value) {
    throw new UnsupportedOperationException("cannot set a value in the empty environment");
  }

  @Override
  public Optional<SEXP> getLocal(String name) {
    return Optional.empty();
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public Optional<Pair<EnvSXP, SEXP>> find(String name) {
    return Optional.empty();
  }

  @Override
  public @UnmodifiableView Set<Entry<String, SEXP>> bindings() {
    return Set.of();
  }

  @Override
  public EnvType envType() {
    return EnvType.EMPTY;
  }

  @Override
  public String toString() {
    return Printer.toString(this);
  }
}
