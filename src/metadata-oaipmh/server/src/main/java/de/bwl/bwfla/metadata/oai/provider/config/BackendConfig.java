/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package de.bwl.bwfla.metadata.oai.provider.config;

import de.bwl.bwfla.common.utils.ConfigHelpers;
import de.bwl.bwfla.metadata.oai.common.config.BaseConfig;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.inject.api.Config;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class BackendConfig extends BaseConfig
{
	private String name;

	private final SourceConfig source = new SourceConfig();
	private final IdentityConfig identity = new IdentityConfig();
	private final ResponseLimitsConfig limits = new ResponseLimitsConfig();


	// ========== Getters and Setters ==============================

	public String getName()
	{
		return name;
	}

	@Config("name")
	public void setName(String name)
	{
		ConfigHelpers.check(name, "Name is invalid!");
		this.name = name;
	}

	public IdentityConfig getIdentityConfig()
	{
		return identity;
	}

	public SourceConfig getSourceConfig()
	{
		return source;
	}

	public ResponseLimitsConfig getResponseLimitsConfig()
	{
		return limits;
	}


	public static class IdentityConfig
	{
		private String repositoryName;
		private String adminEmail;
		private Date earliestDate;


		public String getRepositoryName()
		{
			return repositoryName;
		}

		@Config("repository_name")
		public void setRepositoryName(String name)
		{
			ConfigHelpers.check(name, "Repository name is invalid!");
			this.repositoryName = name;
		}

		public String getAdminEmail()
		{
			return adminEmail;
		}

		@Config("admin_email")
		public void setAdminEmail(String email)
		{
			ConfigHelpers.check(email, "Admin's email is invalid!");
			this.adminEmail = email;
		}

		public Date getEarliestDate()
		{
			return earliestDate;
		}

		@Config("earliest_date")
		public void setEarliestDate(String datestr) throws ParseException
		{
			ConfigHelpers.check(datestr, "Repository's earliest date is invalid!");
			this.earliestDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
					.parse(datestr);
		}
	}

	public static class SourceConfig
	{
		private String baseurl;
		private String secret;


		public String getBaseUrl()
		{
			return baseurl;
		}

		@Config("base_url")
		public void setBaseUrl(String url)
		{
			ConfigHelpers.check(url, "Base URL is invalid!");
			this.baseurl = url;
		}

		public boolean hasSecret()
		{
			return secret != null;
		}

		public String getSecret()
		{
			return secret;
		}

		public void setSecret(String secret)
		{
			if (secret != null && !secret.isEmpty())
				this.secret = secret;
		}
	}

	public static class ResponseLimitsConfig
	{
		private int maxNumIdentifiers;
		private int maxNumRecords;
		private int maxNumSets;


		public int getMaxNumIdentifiers()
		{
			return maxNumIdentifiers;
		}

		@Config("max_num_identifiers")
		public void setMaxNumIdentifiers(int number)
		{
			ConfigHelpers.check(number, 1, 500, "Max. number of identifiers is invalid!");
			this.maxNumIdentifiers = number;
		}

		public int getMaxNumRecords()
		{
			return maxNumRecords;
		}

		@Config("max_num_records")
		public void setMaxNumRecords(int number)
		{
			ConfigHelpers.check(number, 1, 500, "Max. number of records is invalid!");
			this.maxNumRecords = number;
		}

		public int getMaxNumSets()
		{
			return maxNumSets;
		}

		@Config("max_num_sets")
		public void setMaxNumSets(int number)
		{
			ConfigHelpers.check(number, 1, 500, "Max. number of sets is invalid!");
			this.maxNumSets = number;
		}
	}


	// ========== Initialization ==============================

	public void load(Configuration config)
	{
		// Configure annotated members of this instance
		ConfigHelpers.configure(this, config);
		ConfigHelpers.configure(identity, ConfigHelpers.filter(config, "identity."));
		ConfigHelpers.configure(source, ConfigHelpers.filter(config, "source."));
		ConfigHelpers.configure(limits, ConfigHelpers.filter(config, "response_limits."));
	}
}
