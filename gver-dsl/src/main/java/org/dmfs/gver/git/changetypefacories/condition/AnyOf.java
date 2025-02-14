package org.dmfs.gver.git.changetypefacories.condition;

import org.dmfs.gver.git.changetypefacories.Condition;
import org.dmfs.jems2.iterable.Mapped;
import org.dmfs.jems2.optional.First;
import org.dmfs.jems2.single.Backed;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;


public final class AnyOf implements Condition
{

    private final Iterable<? extends Condition> mDelegates;


    public AnyOf(Iterable<? extends Condition> delegates)
    {
        mDelegates = delegates;
    }


    @Override
    public boolean matches(Repository repository, RevCommit commit, String branch)
    {
        return new Backed<>(new First<>(m -> m, new Mapped<>(c -> c.matches(repository, commit, branch), mDelegates)), false).value();
    }
}
