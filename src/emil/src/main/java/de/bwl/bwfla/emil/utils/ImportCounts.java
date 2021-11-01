package de.bwl.bwfla.emil.utils;

import com.openslx.eaas.common.util.MultiCounter;


public enum ImportCounts
{
	IMPORTED,
	FAILED,
	__LAST;

	public static MultiCounter counter()
	{
		return new MultiCounter(__LAST.ordinal());
	}
}
