package de.bwl.bwfla.emil.utils;

import com.openslx.eaas.common.util.AtomicMultiCounter;


public enum ImportCounts
{
	IMPORTED,
	FAILED,
	__LAST;

	public static AtomicMultiCounter counter()
	{
		return new AtomicMultiCounter(__LAST.ordinal());
	}
}
