package net.sourceforge.seqware.common.business;

import java.util.List;
import net.sourceforge.seqware.common.dao.ProcessingIUSDAO;
import net.sourceforge.seqware.common.model.IUS;
import net.sourceforge.seqware.common.model.Processing;
import net.sourceforge.seqware.common.model.ProcessingIus;

public interface ProcessingIUSService {

  public abstract void setProcessingIUSDAO(ProcessingIUSDAO dao);

  public abstract ProcessingIus findByProcessingIUS(Processing processing, IUS ius);

  public abstract void delete(ProcessingIus processingIus);

  public abstract void update(ProcessingIus processingIus);

  public abstract void insert(ProcessingIus processingIus);

  public abstract ProcessingIus updateDetached(ProcessingIus processingIus);
  
  public List<ProcessingIus> list();

}