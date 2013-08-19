package uk.ac.standrews.cs.trombone;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class StaticPeerConfigurator1 implements PeerConfigurator {

    @Override
    public void configure(final Peer peer) {

        final Maintenance maintenance = peer.getMaintenance();
        //        maintenance.addNonOpprotunisticGossip(new NonOpportunisticGossip(peer, First.getInstance(), new OpportunisticGossip(peer, new LookupSelector(peer.getKey().) true, 1), new Duration(2, TimeUnit.SECONDS)));

    }
}
