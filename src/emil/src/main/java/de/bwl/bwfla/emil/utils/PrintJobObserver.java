package de.bwl.bwfla.emil.utils;

import de.bwl.bwfla.api.emucomp.Machine;
import de.bwl.bwfla.common.exceptions.BWFLAException;

import java.util.ArrayList;
import java.util.List;

public class PrintJobObserver implements EventObserver
{
    private String sessionId;
    private int printCount = 0;
    private final Machine machine;

    public PrintJobObserver(Machine machine, String sessionId)
    {
        this.sessionId = sessionId;
        printCount = 0;
        this.machine = machine;
    }

    @Override
    public List<String> messages() {
        List<String> l = new ArrayList<>();

        try {
            List<de.bwl.bwfla.api.emucomp.PrintJob> pj = machine.getPrintJobs(sessionId);

            if(pj.size() > printCount)
            {
                l.add("" + pj.size());
                printCount = pj.size();
            }
        } catch (BWFLAException e) {
            e.printStackTrace();
        }

        return l;
    }

    @Override
    public String getName() {
        return "PrintJobObserver";
    }
}
